package com.sandeep.shrofiletask;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by sandeep on 25-07-2017.
 */

public class PrefManager
{
    public static final String SHARED_PREF_NAME = "FCMSharedPref";
    public static final String TAG_PATH= "PATH";
    public static final String STR_DEFAULT = "DEFAULT";

    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;




    public  static void savePath(Context context,String email) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString(TAG_PATH, email);
        editor.commit();

    }

    public static String getPath(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(TAG_PATH, PrefManager.STR_DEFAULT);
    }


}


