package com.sandeep.shrofiletask;

import android.hardware.Camera;

/**
 * Created by sandeep on 24-07-2017.
 */

public class CamraInstance
{
    private static final int IMAGE_SIZE = 1024;

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(findFrontFacingCamera());// attempt to get a Camera instance
            Camera.Parameters camParams = c.getParameters();
            Camera.Size previewSize = camParams.getPreferredPreviewSizeForVideo();
            for (Camera.Size size : camParams.getSupportedVideoSizes()) {
                if (size.width >= IMAGE_SIZE && size.height >= IMAGE_SIZE) {
                    previewSize = size;
                    break;
                }

                Camera.Size pictureSize = camParams.getPreferredPreviewSizeForVideo();
                for (Camera.Size sizes : camParams.getSupportedVideoSizes()) {
                    if (size.width == previewSize.width && size.height == previewSize.height) {
                        pictureSize = size;
                        break;
                    }
                }
                camParams.setPictureSize(pictureSize.width, pictureSize.height);
            }

        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private static int findFrontFacingCamera() {

        int cameraId = -1;

        int numberOfCameras = Camera.getNumberOfCameras();

        for (int i = 0; i < numberOfCameras; i++) {

            Camera.CameraInfo info = new Camera.CameraInfo();

            Camera.getCameraInfo(i, info);

            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {

                cameraId = i;


                break;

            }

        }

        return cameraId;

    }
}
