package com.tktkcompany.kakoRaceKeiba.ui.chat; // あなたのパッケージ名に合わせてください

import android.os.Bundle;

import android.text.TextUtils;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.tktkcompany.kakoRaceKeiba.BuildConfig;
import com.tktkcompany.kakoRaceKeiba.databinding.FragmentChatBinding; // ViewBindingを使用
// ▼▼▼ Gemini用のDTOをインポート ▼▼▼
import com.tktkcompany.kakoRaceKeiba.dto.GeminiChatRequest;
import com.tktkcompany.kakoRaceKeiba.dto.GeminiChatResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatFragment extends Fragment {
    private static final String TAG = "ChatFragment"; // ログ出力用
    private FragmentChatBinding binding;
    private RecyclerView recyclerViewChat;
    private EditText editTextMessage;
    private ImageButton buttonSend;
    private Button buttonAnalyze; // 【追加】ボタン用のフィールド

    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private DatabaseReference chatDatabaseRef;

    private final String CHAT_ID = "unique_chat_id_001";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Viewの初期化
        recyclerViewChat = binding.recyclerViewChat;
        editTextMessage = binding.editTextMessage;
        buttonSend = binding.buttonSend;
        buttonAnalyze = binding.buttonAnalyze; // 【追加】ボタンを初期化

        // ... (RecyclerViewとFirebaseの初期設定はそのまま) ...
        // RecyclerViewの設定
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerViewChat.setLayoutManager(layoutManager);
        recyclerViewChat.setAdapter(chatAdapter);

        // Firebase Realtime Databaseの参照を設定
        chatDatabaseRef = FirebaseDatabase.getInstance()
                .getReference("chats") // ルートパス
                .child(CHAT_ID)      // チャットごとのID
                .child("messages");  // メッセージリスト

        // 送信ボタンのクリックリスナー
        buttonSend.setOnClickListener(v -> sendMessage());

        // 【追加】要約ボタンのクリックリスナー
        buttonAnalyze.setOnClickListener(v -> analyzeConversation());

        // 過去のメッセージを読み込むリスナーを設定
        listenForMessages();
    }

    // ... (sendMessageメソッドはそのまま) ...

    /**
     * 【新規追加】会話の分析を開始するメソッド
     */
    private void analyzeConversation() {
        buttonAnalyze.setEnabled(false); // 連打防止
        Toast.makeText(getContext(), "会話履歴を読み込んでいます...", Toast.LENGTH_SHORT).show();

        // get() を使って、現在のチャット履歴を一度だけ取得する
        chatDatabaseRef.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(getContext(), "分析対象の会話がありません。", Toast.LENGTH_SHORT).show();
                    buttonAnalyze.setEnabled(true);
                    return;
                }

                // 1. 取得したデータを文字列に変換する
                StringBuilder conversationHistory = new StringBuilder();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatMessage message = snapshot.getValue(ChatMessage.class);
                    if (message != null) {
                        // "user: こんにちは" のような形式で会話を組み立てる
                        conversationHistory.append(message.getRole())
                                .append(": ")
                                .append(message.getText())
                                .append("\n");
                    }
                }

                // 2. AIへの指示書（プロンプト）を作成する
                String prompt = "あなたは優秀なアシスタントです。以下の会話履歴を簡潔に要約してください。\n\n--- 会話履歴 ---\n"
                        + conversationHistory.toString()
                        + "\n--- ここまで ---\n\n要約:";

                // 3. プロンプトをAPIに送信する
                callGeminiApiWithPrompt(prompt);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "データの読み込みに失敗しました。", Toast.LENGTH_SHORT).show();
            buttonAnalyze.setEnabled(true);
        });
    }


    /**
     * 【新規追加】カスタムプロンプトでGemini APIを呼び出すメソッド
     * @param prompt AIに送る指示全文
     */
    private void callGeminiApiWithPrompt(String prompt) {
        Toast.makeText(getContext(), "AIが分析中です...", Toast.LENGTH_SHORT).show();

        GeminiApiService apiService = ApiClient.getClient().create(GeminiApiService.class);

        GeminiChatRequest.Part part = new GeminiChatRequest.Part(prompt);
        GeminiChatRequest.Content content = new GeminiChatRequest.Content(Collections.singletonList(part));
        GeminiChatRequest request = new GeminiChatRequest(Collections.singletonList(content));

        apiService.generateContent("gemini-2.0-flash", BuildConfig.GEMINI_API_KEY, request)
                .enqueue(new Callback<GeminiChatResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<GeminiChatResponse> call, @NonNull Response<GeminiChatResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                String botResponseText = response.body().getCandidates().get(0).getContent().getParts().get(0).getText();
                                // ★ roleを "assistant" にするなど、通常の発言と区別しても良い
                                ChatMessage botMessage = new ChatMessage("【要約結果】\n" + botResponseText, "assistant");
                                // 結果をFirebaseに書き込む（→listenForMessagesが検知して表示する）
                                chatDatabaseRef.push().setValue(botMessage);
                            } catch (Exception e) {
                                ChatMessage errorMessage = new ChatMessage("応答の解析に失敗しました。", "assistant");
                                chatDatabaseRef.push().setValue(errorMessage);
                            }
                        } else {
                            ChatMessage errorMessage = new ChatMessage("申し訳ありません、分析中にAPIエラーが発生しました。(" + response.code() + ")", "assistant");
                            chatDatabaseRef.push().setValue(errorMessage);
                        }
                        buttonAnalyze.setEnabled(true); // ボタンを再度有効化
                    }

                    @Override
                    public void onFailure(@NonNull Call<GeminiChatResponse> call, @NonNull Throwable t) {
                        ChatMessage errorMessage = new ChatMessage("分析中に通信エラーが発生しました。", "assistant");
                        chatDatabaseRef.push().setValue(errorMessage);
                        buttonAnalyze.setEnabled(true); // ボタンを再度有効化
                    }
                });
    }
    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (!TextUtils.isEmpty(messageText)) {
            // Geminiのroleに合わせて "user" とする
            ChatMessage userMessage = new ChatMessage(messageText, "user");
            chatDatabaseRef.push().setValue(userMessage);

            // 【変更】Gemini APIを呼び出す
            callGeminiApi(messageText);

            editTextMessage.setText("");
        }
    }

    /**
     * 【全面改修】Google Gemini APIを呼び出すメソッド
     * @param userMessageText ユーザーが入力したメッセージ
     */
    private void callGeminiApi(String userMessageText) {
        buttonSend.setEnabled(false); // 連打防止

        // APIクライアントとサービスを取得
        GeminiApiService apiService = ApiClient.getClient().create(GeminiApiService.class);

        // Gemini API用のリクエストボディを作成
        GeminiChatRequest.Part part = new GeminiChatRequest.Part(userMessageText);
        GeminiChatRequest.Content content = new GeminiChatRequest.Content(Collections.singletonList(part));
        GeminiChatRequest request = new GeminiChatRequest(Collections.singletonList(content));

        // APIを非同期で呼び出し
        apiService.generateContent("gemini-2.0-flash", BuildConfig.GEMINI_API_KEY, request)
                .enqueue(new Callback<GeminiChatResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<GeminiChatResponse> call, @NonNull Response<GeminiChatResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                // Geminiのレスポンスからテキストを抽出
                                String botResponseText = response.body().getCandidates().get(0).getContent().getParts().get(0).getText();
                                // Geminiのroleは "model"
                                ChatMessage botMessage = new ChatMessage(botResponseText, "model");
                                chatDatabaseRef.push().setValue(botMessage);
                            } catch (Exception e) {
                                Log.e(TAG, "Response parsing error", e);
                                // パース失敗時のエラーメッセージ
                                ChatMessage errorMessage = new ChatMessage("応答の解析に失敗しました。", "model");
                                chatDatabaseRef.push().setValue(errorMessage);
                            }
                        } else {
                            // APIからのエラーレスポンス (429等)
                            Log.e(TAG, "API Error: " + response.code() + " " + response.message());
                            ChatMessage errorMessage = new ChatMessage("申し訳ありません、APIエラーが発生しました。(" + response.code() + ")", "model");
                            chatDatabaseRef.push().setValue(errorMessage);
                        }
                        buttonSend.setEnabled(true); // ボタンを有効化
                    }

                    @Override
                    public void onFailure(@NonNull Call<GeminiChatResponse> call, @NonNull Throwable t) {
                        // 通信失敗時のエラーハンドリング
                        Log.e(TAG, "API Failure: " + t.getMessage(), t);
                        ChatMessage errorMessage = new ChatMessage("通信に失敗しました。ネットワークを確認してください。", "model");
                        chatDatabaseRef.push().setValue(errorMessage);
                        buttonSend.setEnabled(true); // ボタンを有効化
                    }
                });
    }

    private void listenForMessages() {
        chatDatabaseRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                try {
                    ChatMessage chatMessage = snapshot.getValue(ChatMessage.class);
                    if (chatMessage != null) {
                        messageList.add(chatMessage);
                        chatAdapter.notifyItemInserted(messageList.size() - 1);
                        recyclerViewChat.scrollToPosition(messageList.size() - 1);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse chat message.", e);
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "listenForMessages:onCancelled", error.toException());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}