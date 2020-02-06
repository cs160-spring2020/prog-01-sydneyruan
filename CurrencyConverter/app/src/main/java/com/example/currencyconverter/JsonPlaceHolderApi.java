package com.example.currencyconverter;

import retrofit2.Call;
import retrofit2.http.GET;

import java.util.*;

public interface JsonPlaceHolderApi {
    @GET("posts")
    Call<List<Post>> getPosts();
}
