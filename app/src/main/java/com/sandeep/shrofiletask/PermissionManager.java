package com.sandeep.shrofiletask;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

/**
 * Created by sandeep on 25-07-2017.
 */

public class PermissionManager
{
    public static boolean hasPermissions(Activity activity, String... permissions)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity != null && permissions != null)
        {
            for (String permission : permissions)
            {
                if (ActivityCompat.checkSelfPermission(activity,permission) != PackageManager.PERMISSION_GRANTED)
                    return false;
            }
        }
        return true;
    }
}
