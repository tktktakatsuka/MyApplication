package com.tktkcompany.kakoRaceKeiba.ui.chat;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // 【変更】Gemini APIのベースURLに変更
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/";

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)  // 接続タイムアウト
                .readTimeout(60, TimeUnit.SECONDS)     // 読み取りタイムアウト
                .writeTimeout(60, TimeUnit.SECONDS)    // 書き込みタイムアウト
                .build();

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
        }
        return retrofit;
    }
}