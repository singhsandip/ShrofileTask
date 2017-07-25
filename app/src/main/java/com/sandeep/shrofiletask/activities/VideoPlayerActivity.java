package com.sandeep.shrofiletask.activities;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.sandeep.shrofiletask.PrefManager;
import com.sandeep.shrofiletask.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class VideoPlayerActivity extends AppCompatActivity implements MediaPlayer.OnErrorListener,
        MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, SurfaceHolder.Callback,View.OnClickListener{

    private static final String TAG = "VideoPlayerActivity";

    private MediaPlayer mp;
    private SurfaceView mPreview;
    private SurfaceHolder holder;
    private Button mPlay;
    private String current;
    private boolean isPlaying = false;
    private boolean shoulShowDialog = true;
    private ProgressDialog progressDialog;
    private String videopath ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        videopath = PrefManager.getPath(this);

        Log.d(TAG, "onCreate: "+videopath);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Buffering");
        mPreview = (SurfaceView) findViewById(R.id.surface_view);


        mPlay = (Button) findViewById(R.id.btnPlay);


        mPlay.setOnClickListener(this);

        // Set the transparency
        getWindow().setFormat(PixelFormat.TRANSPARENT);

        // Set a size for the video screen
        holder = mPreview.getHolder();
        holder.addCallback(this);
        holder.setFixedSize(400, 300);

    }



    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {


    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {

            case R.id.btnPlay:

                if (isPlaying)
                {
                    if (mp != null) {
                        mp.pause();
                        mPlay.setText("Pause");
                        isPlaying = false;
                    }
                }
                else
                {
                    isPlaying = true;
                    playVideo();
                    mPlay.setText("Play");

                }

                break;







        }
    }



    private void playVideo()
    {
        try {
            if (shoulShowDialog) {
                progressDialog.show();
            }
            //final String path = mPath.getText().toString();
            final String path = "https://functional-capacity.000webhostapp.com/"+videopath;
            Log.e(TAG, "path: " + path);

            // If the path has not changed, just start the media player
            if (path.equals(current) && mp != null) {
                mp.start();
                return;
            }
            current = path;

            // Create a new media player and set the listeners
            mp = new MediaPlayer();
            mp.setOnErrorListener(this);
            mp.setOnBufferingUpdateListener(this);
            mp.setOnCompletionListener(this);
            mp.setOnPreparedListener(this);
            mp.setAudioStreamType(2);

            // Set the surface for the video output
            mp.setDisplay(holder);

            // Set the data source in another thread
            // which actually downloads the mp3 or videos
            // to a temporary location
            Runnable r = new Runnable() {
                public void run() {
                    try {
                        setDataSource(path);
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                    try {
                        mp.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.v(TAG, "Duration:  ===>" + mp.getDuration());
                    mp.start();
                }
            };
            new Thread(r).start();
        } catch (Exception e) {
            Log.e(TAG, "error: " + e.getMessage(), e);
            if (mp != null) {
                mp.stop();
                mp.release();
            }
        }
    }

    private void setDataSource(String path) throws IOException {
        if (!URLUtil.isNetworkUrl(path)) {
            mp.setDataSource(path);
        } else {
            URL url = new URL(path);
            URLConnection cn = url.openConnection();
            cn.connect();
            InputStream stream = cn.getInputStream();
            if (stream == null)
                throw new RuntimeException("stream is null");
            File temp = File.createTempFile("mediaplayertmp", "dat");
            String tempPath = temp.getAbsolutePath();
            FileOutputStream out = new FileOutputStream(temp);
            byte buf[] = new byte[128];
            do {
                int numread = stream.read(buf);
                if (numread <= 0)
                    break;
                out.write(buf, 0, numread);
            } while (true);
            mp.setDataSource(tempPath);
            try {
                stream.close();
               // tvTimerViwer.setVisibility(View.GONE);
            }
            catch (IOException ex) {
                Log.e(TAG, "error: " + ex.getMessage(), ex);
            }
        }
        progressDialog.dismiss();
        shoulShowDialog = false;

    }






}
