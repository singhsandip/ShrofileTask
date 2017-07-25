package com.sandeep.shrofiletask;

import retrofit.RestAdapter;

/**
 * Created by sandeep on 25-07-2017.
 */

public class RetrofitInstance
{
    public static final String BASE_URL = "https://functional-capacity.000webhostapp.com";
    public static RestAdapter retrofit = null;




    public static RestAdapter getRetrofit() {
        if (retrofit==null) {
            retrofit = new RestAdapter.Builder()
                    .setEndpoint(BASE_URL)
                    .build();
        }
        return retrofit;
    }
}
