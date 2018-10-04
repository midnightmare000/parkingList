package com.example.sally.parkingapp.util;

import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by AndyShiu on 2017/4/15.
 */

public class Network {



    public static void getData(final String url, final Map<String,String> kv, final Callback callback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                if(kv!=null) {
                    for (String key : kv.keySet()) {
                        builder = builder.addFormDataPart(key, kv.get(key));
                    }
                }else{
                    builder = builder.addFormDataPart("null", "null");
                }

                RequestBody body = builder.build();
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                Call call = client.newCall(request);
                call.enqueue(callback);


            }
        }).start();


    }

    public static void postData(final String url, final Map<String,String> kv, final Callback callback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                if(kv!=null) {
                    for (String key : kv.keySet()) {
                        builder = builder.addFormDataPart(key, kv.get(key));
                    }
                }else{
                    builder = builder.addFormDataPart("null", "null");
                }

                RequestBody body = builder.build();
                Request request = new Request.Builder()
                        .url(url)
                        .method("POST", RequestBody.create(null, new byte[0]))
                        .post(body)
                        .build();
                Call call = client.newCall(request);
                call.enqueue(callback);


            }
        }).start();


    }
}
