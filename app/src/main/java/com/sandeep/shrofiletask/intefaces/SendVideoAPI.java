package com.sandeep.shrofiletask.intefaces;

import com.sandeep.shrofiletask.VideoUploadResponse;

import retrofit.Callback;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;

/**
 * Created by sandeep on 25-07-2017.
 */

public interface SendVideoAPI
{
    @Multipart
    @POST("/uloadVideo.php")
    public void postChore(
            @Part("uploadedfile") TypedFile video,
            Callback<VideoUploadResponse> callback);
}
