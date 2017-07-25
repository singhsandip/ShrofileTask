package com.sandeep.shrofiletask;

import android.hardware.Camera;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by sandeep on 24-07-2017.
 */

public class ScreenResolution
{

    private static ArrayList<Integer> arrayListForWidth= new ArrayList<Integer>();;
    private static ArrayList<Integer> arrayListForHeight = new ArrayList<Integer>();;


    public static int getMaxHeight()
    {
        int maxHeight = 0;
        if(arrayListForHeight.size() != 0 ){

            maxHeight =  Collections.max(arrayListForHeight);
        }
        return maxHeight;
    }

    private static int getMaxWidth(Camera mCamera)
    {
        int maxwidth = 0;
        // Camera c = Camera.open(findFrontFacingCamera());
        Camera.Parameters cpram = mCamera.getParameters();
        List sizes = cpram.getSupportedVideoSizes();
        Camera.Size  result = null;



        for (int i=0;i<sizes.size();i++){
            result = (Camera.Size) sizes.get(i);
            arrayListForWidth.add(result.width);
            arrayListForHeight.add(result.height);
            Log.d("PictureSize", "Supported Size: width " + result.width + " height : " + result.height);
            // System.out.println("BACK PictureSize Supported Size: " + result.width + "height : " + result.height);
        }
        if(arrayListForWidth.size() != 0 ){

            maxwidth =  Collections.max(arrayListForWidth);
            Log.d("FRONT max W :", String.valueOf(Collections.max(arrayListForWidth)));
            Log.d("FRONT max H :", String.valueOf(Collections.max(arrayListForHeight)));
            Log.d("FRONT Megapixel :", String.valueOf(( ((Collections.max(arrayListForWidth)) * (Collections.max(arrayListForHeight))) / 1024000 )));
        }

        return maxwidth;
    }

}
