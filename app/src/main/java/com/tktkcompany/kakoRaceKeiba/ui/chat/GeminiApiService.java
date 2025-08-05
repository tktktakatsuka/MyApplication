package com.tktkcompany.kakoRaceKeiba.ui.chat;

import com.tktkcompany.kakoRaceKeiba.BuildConfig;
import com.tktkcompany.kakoRaceKeiba.dto.GeminiChatRequest;
import com.tktkcompany.kakoRaceKeiba.dto.GeminiChatResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GeminiApiService {
    /**
     * Gemini APIにリクエストを送信します。
     * @param modelName 使用するモデル (例: "gemini-pro")
     * @param apiKey APIキー。@QueryアノテーションでURLの末尾に ?key=... として付与されます。
     * @param request リクエストボディ
     * @return APIからのレスポンス
     */
    @POST("v1beta/models/{modelName}:generateContent")
    Call<GeminiChatResponse> generateContent(
            @retrofit2.http.Path("modelName") String modelName,
            @Header("x-goog-api-key") String apiKey,
            @Body GeminiChatRequest request
    );
}