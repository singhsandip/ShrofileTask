package com.sandeep.shrofiletask.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.sandeep.shrofiletask.CamraInstance;
import com.sandeep.shrofiletask.CheckInternetConnection;
import com.sandeep.shrofiletask.PermissionManager;
import com.sandeep.shrofiletask.PrefManager;
import com.sandeep.shrofiletask.R;
import com.sandeep.shrofiletask.RetrofitInstance;
import com.sandeep.shrofiletask.VideoUploadResponse;
import com.sandeep.shrofiletask.intefaces.SendVideoAPI;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

import static android.os.Build.VERSION_CODES.M;


public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback
{
    private static final String TAG = "camera";

    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private MediaRecorder mMediaRecorder;
    private boolean isSameVideo = false;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private SurfaceHolder mHolder;
    private RelativeLayout overlay;
    private String videopath = "";
    private static File mediafile = null;
    private StringBuilder ffmpegCommand = null;
    private FFmpeg ffmpeg;
    private int mFileHeight = 200;
    private int mFileY = 200;
    private int mFileWidth = 200;
    private boolean isPause = false;
    private ProgressDialog progressDailog;
    private Context context;
    private TextView timerValue;
    private long startTime = 1L;
    private Handler customHandler = new Handler();
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;
    private Button btnRecording;
    private boolean isRecording = false;
    private String outputVideoPath;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        intialize();

    }




    private void intialize() {
        context = this;
        progressDailog = new ProgressDialog(this);

        timerValue = (TextView) findViewById(R.id.tvTimerValue);
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


        loadFFmpeg();


        overlay = (RelativeLayout) findViewById(R.id.overlay);


        // we shall take the video in landscape orientation
        //  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mCamera = CamraInstance.getCameraInstance();
        mCamera.setDisplayOrientation(90);



        btnRecording = (Button) findViewById(R.id.RecordingButton);


        btnRecording.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isRecording) {
                            // stop recording and release camera
                            mMediaRecorder.stop();  // stop the recording
                            releaseMediaRecorder(); // release the MediaRecorder object
                            mCamera.lock();         // take camera access back from MediaRecorder

                            // inform the user that recording has stopped
                            timeSwapBuff += timeInMilliseconds;
                            customHandler.removeCallbacks(updateTimerThread);
                            timerValue.setVisibility(View.GONE);

                            isRecording = false;
                        } else {
                            // initialize video camera
                            if (prepareVideoRecorder()) {
                                // Camera is available and unlocked, MediaRecorder is prepared,
                                // now you can start recording

                                mMediaRecorder.start();

                                // inform the user that recording has started
                                timerValue.setVisibility(View.VISIBLE);
                                startTime = SystemClock.uptimeMillis();
                                customHandler.postDelayed(updateTimerThread, 0);
                                isRecording = true;
                            } else {
                                // prepare didn't work, release the camera
                                releaseMediaRecorder();
                                // inform user
                            }
                        }
                    }
                });
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);


        // Get the preview size
        int previewWidth = mSurfaceView.getMeasuredWidth(),
                previewHeight = mSurfaceView.getMeasuredHeight();

        // Set the height of the overlay so that it makes the preview a square
        RelativeLayout.LayoutParams overlayParams = (RelativeLayout.LayoutParams) overlay.getLayoutParams();
        overlayParams.height = previewHeight - previewWidth;

        mFileWidth = previewWidth;
        mFileHeight = previewHeight;
        mFileY = overlayParams.height = previewHeight - previewWidth;
        overlay.setLayoutParams(overlayParams);

    }

    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
      //  File mediaFile;

         if(type == MEDIA_TYPE_VIDEO) {
            mediafile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediafile;
    }

    private boolean prepareVideoRecorder(){

        mMediaRecorder = new MediaRecorder();

        Log.e("outside camera", String.valueOf(mCamera));

        /*if (mCamera == null)
        {
            Log.e("null camera", String.valueOf(mCamera));
        }
        else
        {*/
            mCamera.unlock();
            mMediaRecorder.setCamera(mCamera);

        //}
        // Step 1: Unlock and set camera to MediaRecorder

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
//        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setVideoEncodingBitRate(690000);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
        mMediaRecorder.setVideoSize(720,480);


        videopath = getOutputMediaFileUri(MEDIA_TYPE_VIDEO).getPath();

        Log.e(TAG, "videopath : "+videopath );

        mMediaRecorder.setOutputFile(videopath);

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }


    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
        releaseCamera();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }





    private void loadFFmpeg() {

        Log.d(TAG, "onFailure: "+"load enterd");
        ffmpeg = FFmpeg.getInstance(this);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {
                    Log.d(TAG, "onFailure: "+"load started");
                }

                @Override
                public void onFailure() {
                    Log.d(TAG, "onFailure: "+"load failde");
                }

                @Override
                public void onSuccess() {
                    Log.d(TAG, "onFailure: "+"load success");
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "onFailure: "+"load finished");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            // Handle if FFmpeg is not supported by device
        }
    }

    private File makeOutputFile()
    {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCroppedVideos");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");

            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        //  File mediaFile;

        File  outputfile = new File(mediaStorageDir.getPath() + File.separator +
                "VID"+ timeStamp + ".mp4");
        Log.d(TAG, "cropVideo: path = "+outputfile.getAbsolutePath());
        return outputfile;


    }



    private void cropAndCompreesVideo()
    {
        outputVideoPath = makeOutputFile().getAbsolutePath();

        progressDailog.setMessage("Croping Video...");
        progressDailog.show();
       String[] complexCommand = {"-i", videopath, "-vf","scale=480:320","-vf","transpose=2","-vf", "crop="+mFileY+":480:"+mFileY+":0" ,outputVideoPath};



        try {
            final FFmpeg ffmpeg = FFmpeg.getInstance(context);
            ffmpeg.execute(complexCommand, new FFmpegExecuteResponseHandler() {
                @Override
                public void onSuccess(String message) {
                    Log.d(TAG, "onSuccess: "+ message);
                    progressDailog.dismiss();
                   // compressVideo(videopath);
                    Toast.makeText(getApplicationContext(), "Successfully Croped!",
                            Toast.LENGTH_SHORT).show();
                    showAlertDialog();
                }

                @Override
                public void onProgress(String message) {


                    Log.d(TAG, "onSuccessprg: "+ message);
                }

                @Override
                public void onFailure(String message) {
                    Log.d(TAG, "onSuccessfail: "+ message);
                    progressDailog.dismiss();
                    Toast.makeText(getApplicationContext(), "Failed To Crop!",
                            Toast.LENGTH_LONG).show();
                }

                @Override
                public void onStart() {
                    //Toast.makeText(getApplicationContext(), "Started!",
                      //      Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFinish() {
                    progressDailog.dismiss();
                   // Log.d(TAG, "onSuccess: "+ message);
                    //Toast.makeText(getApplicationContext(), "Stopped!",
                      //      Toast.LENGTH_LONG).show();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {

        }
    }








    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release();// release the recorder object
         //   Toast.makeText(this, "Your Video is saved click for another", Toast.LENGTH_SHORT).show();
            mMediaRecorder = null;
            mCamera.lock();
            /*if (isPause)
            {
                showAlertDialog();
                isPause = false;
            }*/
            //cropVideoNew(videopath,videopath,100,100,mFileWidth,mFileHeight,0);
          cropAndCompreesVideo();// lock camera for later use
}
    }

    private Runnable updateTimerThread = new Runnable() {

        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;

            updatedTime = timeSwapBuff + timeInMilliseconds;

            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            //int milliseconds = (int) (updatedTime % 1000);
            timerValue.setText("" + mins + ":"
                    + String.format("%02d", secs));// + ":"
            //+ String.format("%03d", milliseconds));
            customHandler.postDelayed(this, 0);
        }

    };

    private void showAlertDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do You Want To Upload Video!")
                .setCancelable(false)
                .setPositiveButton("Upload", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        progressDailog.setMessage("Uploading....");
                        progressDailog.show();
                        if (CheckInternetConnection.isConnecetedToInternet(MainActivity.this))
                        {
                            upLoadVideo(outputVideoPath);
                        }
                        else
                        {
                            Toast.makeText(context, "Please connect to internet", Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });


        AlertDialog alert = builder.create();
        alert.show();
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    private void upLoadVideo(String outputVideoPath)
    {
        progressDailog.setMessage("uploading.....");
        progressDailog.show();
        String mimetype = getMimeType(outputVideoPath);
        File file = new File(outputVideoPath);
        TypedFile video = new TypedFile(mimetype,file);
        SendVideoAPI sendVideo = RetrofitInstance.getRetrofit().create(SendVideoAPI.class);
        sendVideo.postChore(video, new Callback<VideoUploadResponse>() {
            @Override
            public void success(VideoUploadResponse videoUploadResponse, Response response) {

                if (CheckInternetConnection.isConnecetedToInternet(MainActivity.this))
                {
                    String message = videoUploadResponse.getMessage().toString();
                    if (message.equalsIgnoreCase("file successfully uploaded"))
                    {
                        progressDailog.dismiss();
                        Toast.makeText(context, ""+message, Toast.LENGTH_SHORT).show();
                        PrefManager.savePath(MainActivity.this,videoUploadResponse.getPath().toString());
                        startActivity(new Intent(MainActivity.this,VideoPlayerActivity.class));
                    }
                    else
                    {
                        progressDailog.dismiss();
                        Toast.makeText(context, "Error uploading"+videoUploadResponse.getMessage(), Toast.LENGTH_SHORT).show();

                    }

                }
                else
                {
                    Toast.makeText(context, "Please Check Internet Cnnection", Toast.LENGTH_SHORT).show();
                }




            }

            @Override
            public void failure(RetrofitError error) {

                progressDailog.dismiss();
                Toast.makeText(context, "failed Network Issue!", Toast.LENGTH_SHORT).show();
                showDialogAgain();
                Log.e(TAG, "failure: "+error );

            }
        });
    }

    private void showDialogAgain() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Want To Upload Again");
        builder.setMessage("Do You Want To Upload Video!")
                .setCancelable(false)
                .setPositiveButton("Upload", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        progressDailog.setMessage("Uploading....");
                        progressDailog.show();
                        if (CheckInternetConnection.isConnecetedToInternet(MainActivity.this))
                        {
                            upLoadVideo(outputVideoPath);
                        }
                        else
                        {
                            Toast.makeText(context, "Please connect to internet", Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });


        AlertDialog alert = builder.create();
        alert.show();
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

}