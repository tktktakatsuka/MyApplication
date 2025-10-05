package com.tktkcompany.kakoRaceKeiba.ui.chat; // あなたのパッケージ名に合わせてください

import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tktkcompany.kakoRaceKeiba.dto.ChatMessage;
import com.tktkcompany.kakoRaceKeiba.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tktkcompany.kakoRaceKeiba.BuildConfig;
import com.tktkcompany.kakoRaceKeiba.databinding.FragmentChatBinding;

import java.text.SimpleDateFormat;

import com.tktkcompany.kakoRaceKeiba.dto.GeminiChatRequest;
import com.tktkcompany.kakoRaceKeiba.dto.GeminiChatResponse;
import com.tktkcompany.kakoRaceKeiba.dto.SharedViewModel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


import java.io.File;


public class ChatFragment extends Fragment implements ChatAdapter.OnShareButtonClickListener {
    private static final String TAG = "ChatFragment"; // ログ出力用
    private FragmentChatBinding binding;
    private RecyclerView recyclerViewChat;
    private EditText editTextMessage;
    private ImageButton buttonSend;
    private Button buttonAnalyze;
    private Button buttonAnalyzeRace;
    private DatabaseReference raceResultDatabaseRef;
    private SharedViewModel sharedViewModel;
    private String currentUserId; // ログインユーザーのIDを保持する変数
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private DatabaseReference chatDatabaseRef;
    private ChildEventListener chatEventListener; // ★ リスナーをメンバー変数として保持

    // ▼▼▼【追加】SharedPreferences用の定数 ▼▼▼
    private static final String PREFS_NAME = "ApiUsagePrefs";
    private static final String KEY_USAGE_COUNT = "usage_count";
    private static final String KEY_LAST_USAGE_DATE = "last_usage_date";
    private static final int DAILY_LIMIT = 4; // 1日の利用上限回数


    private Map<String, RadioButton> venueRadioButtons;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- Viewの初期化 ---
        recyclerViewChat = binding.recyclerViewChat;
        editTextMessage = binding.editTextMessage;
        buttonSend = binding.buttonSend;
        buttonAnalyze = binding.buttonAnalyze;
        buttonAnalyzeRace = binding.buttonAnalyzeRace;
        initializeRadioButtons();

        // ★★★ 1. 初期状態では分析ボタンを無効にしておく ★★★
        buttonAnalyze.setEnabled(false);
        buttonAnalyzeRace.setEnabled(false);

        // --- RecyclerViewの設定 ---
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerViewChat.setLayoutManager(layoutManager);
        recyclerViewChat.setAdapter(chatAdapter);
        chatAdapter.setOnShareButtonClickListener(this);

        // --- ★★★ 固定値のDB参照を削除 ★★★ ---
        raceResultDatabaseRef = FirebaseDatabase.getInstance().getReference("raceResult");

        // --- ViewModelのUIDを監視 ---
        sharedViewModel.getUid().observe(getViewLifecycleOwner(), uid -> {
            if (uid != null && !uid.isEmpty()) {
                // UIDが変更された場合（または初回取得時）
                if (!uid.equals(currentUserId)) {
                    this.currentUserId = uid;
                    Log.d(TAG, "UID received in ChatFragment: " + this.currentUserId);
                    initializeDatabaseForUser();
                    // ★★★ 2. ログイン状態になったら、すべての関連ボタンを有効化 ★★★
                    buttonSend.setEnabled(true);
                    buttonAnalyze.setEnabled(true);
                    buttonAnalyzeRace.setEnabled(true);
                    editTextMessage.setHint("メッセージを入力..."); // ヒントテキストを元に戻す
                }
            } else {
                // UIDがnullの場合（未ログイン/ログアウト時）
                Log.w(TAG, "UID is null. Clearing chat.");
                cleanupDatabaseListener(); // リスナーを解除
                messageList.clear();
                chatAdapter.notifyDataSetChanged();
                buttonSend.setEnabled(false);
                editTextMessage.setHint("チャットを利用するにはログインが必要です");
                Toast.makeText(getContext(), "ログインしていません", Toast.LENGTH_LONG).show();
            }
        });

        // --- クリックリスナーの設定 ---
        buttonSend.setOnClickListener(v -> sendMessage());
        buttonAnalyze.setOnClickListener(v -> analyzeConversation());
        buttonAnalyzeRace.setOnClickListener(v -> analyzeRaceResults());
    }

    /**
     * RadioButtonウィジェットを初期化し、マップに格納するメソッド
     */
    private void initializeRadioButtons() {
        venueRadioButtons = new HashMap<>();
        // 第1引数の文字列は、Firebaseのキー名と完全に一致させる
        venueRadioButtons.put("中京", binding.checkboxChukyo); // XMLのIDはそのまま利用
        venueRadioButtons.put("札幌", binding.checkboxTokyo);
        venueRadioButtons.put("新潟", binding.checkboxKyoto);
        venueRadioButtons.put("阪神", binding.checkboxHanshin);
        venueRadioButtons.put("中山", binding.checkboxNakayama);
    }

    /**
     * 会話の分析を開始するメソッド
     */
    private void analyzeConversation() {
        // ★★★ 利用回数チェック ★★★
        if (!isApiUsageAllowed()) {
            Toast.makeText(getContext(), "本日の分析機能の利用上限（" + DAILY_LIMIT + "回）に達しました。", Toast.LENGTH_LONG).show();
            return; // 処理を中断
        }
        // ★★★ 利用回数をインクリメント ★★★
        incrementApiUsageCount();

        buttonAnalyze.setEnabled(false);
        buttonAnalyzeRace.setEnabled(false);
        Toast.makeText(getContext(), "会話履歴を読み込んでいます...", Toast.LENGTH_SHORT).show();

        chatDatabaseRef.get().addOnSuccessListener(dataSnapshot -> {
            if (!dataSnapshot.exists()) {
                Toast.makeText(getContext(), "分析対象の会話がありません。", Toast.LENGTH_SHORT).show();
                buttonAnalyze.setEnabled(true);
                buttonAnalyzeRace.setEnabled(true);
                return;
            }

            StringBuilder conversationHistory = new StringBuilder();
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                ChatMessage message = snapshot.getValue(ChatMessage.class);
                if (message != null) {
                    conversationHistory.append(message.getRole())
                            .append(": ")
                            .append(message.getText())
                            .append("\n");
                }
            }

            String prompt = "あなたは優秀なアシスタントです。以下の会話履歴を簡潔に要約してください。\n\n--- 会話履歴 ---\n"
                    + conversationHistory.toString()
                    + "\n--- ここまで ---\n\n要約:";

            callGeminiApiWithPrompt(prompt);
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "データの読み込みに失敗しました。", Toast.LENGTH_SHORT).show();
            buttonAnalyze.setEnabled(true);
            buttonAnalyzeRace.setEnabled(true);
        });
    }

    /**
     * 選択されたラジオボタンの値でレース結果を絞り込んで分析するメソッド
     */
    private void analyzeRaceResults() {

        // ★★★ 利用回数チェック ★★★
        if (!isApiUsageAllowed()) {
            Toast.makeText(getContext(), "本日の分析機能の利用上限（" + DAILY_LIMIT + "回）に達しました。", Toast.LENGTH_LONG).show();
            return; // 処理を中断
        }
        // ★★★ 利用回数をインクリメント ★★★
        incrementApiUsageCount();
        buttonAnalyze.setEnabled(false);
        buttonAnalyzeRace.setEnabled(false);

        // 1. 選択されているラジオボタンの競馬場名を取得する
        String selectedVenue = null;
        for (Map.Entry<String, RadioButton> entry : venueRadioButtons.entrySet()) {
            if (entry.getValue().isChecked()) {
                selectedVenue = entry.getKey();
                break;
            }
        }

        // 2. 競馬場が何も選択されていない場合、ユーザーに通知して処理を中断する
        if (selectedVenue == null) {
            Toast.makeText(getContext(), "分析する競馬場を1つ選択してください。", Toast.LENGTH_LONG).show();
            buttonAnalyze.setEnabled(true);
            buttonAnalyzeRace.setEnabled(true);
            return;
        }

        Toast.makeText(getContext(), "過去のレース結果を読み込んでいます...", Toast.LENGTH_SHORT).show();
        final String finalSelectedVenue = selectedVenue;

        // 3. Firebaseから全レース結果データを一度に取得する
        raceResultDatabaseRef.get().addOnSuccessListener(dataSnapshot -> {
            if (!dataSnapshot.exists()) {
                Toast.makeText(getContext(), "分析対象のレース結果がありません。", Toast.LENGTH_SHORT).show();
                buttonAnalyze.setEnabled(true);
                buttonAnalyzeRace.setEnabled(true);
                return;
            }

            StringBuilder raceDataText = new StringBuilder();
            raceDataText.append("【").append(finalSelectedVenue).append("競馬場の過去レース結果データ】\n\n");
            boolean dataFoundForSelectedVenue = false;

            // 4. 取得した全データの中から、選択された競馬場に一致するものだけを処理する
            for (DataSnapshot venueSnapshot : dataSnapshot.getChildren()) {
                String currentVenueName = venueSnapshot.getKey();

                // 現在ループしている競馬場名が、選択された競馬場名と一致するかどうかをチェック
                if (finalSelectedVenue.equals(currentVenueName)) {
                    dataFoundForSelectedVenue = true;
                    raceDataText.append("■■■ 競馬場: ").append(currentVenueName).append(" ■■■\n");

                    for (DataSnapshot raceSnapshot : venueSnapshot.getChildren()) {
                        String raceTitle = raceSnapshot.child("raceTitle").getValue(String.class);
                        String horseName = raceSnapshot.child("horseName").getValue(String.class);
                        String jockey = raceSnapshot.child("jockey").getValue(String.class);
                        String popular = raceSnapshot.child("popular").getValue(String.class);
                        String winOdds = raceSnapshot.child("winOdds").getValue(String.class);
                        String tyaku = raceSnapshot.child("tyaku").getValue(String.class);

                        if (raceTitle != null)
                            raceDataText.append("・レース名: ").append(raceTitle).append("\n");
                        if (horseName != null)
                            raceDataText.append("  馬名: ").append(horseName).append("\n");
                        if (jockey != null)
                            raceDataText.append("  騎手: ").append(jockey).append("\n");
                        if (popular != null)
                            raceDataText.append("  人気: ").append(popular).append("番人気\n");
                        if (winOdds != null)
                            raceDataText.append("  単勝オッズ: ").append(winOdds).append("\n");
                        if (tyaku != null)
                            raceDataText.append("  着順: ").append(tyaku).append("着\n");
                        raceDataText.append("\n");
                    }
                    break; // 目的の競馬場の処理が終わったので、ループを抜ける
                }
            }

            // 5. 選択された競馬場のデータがデータベースに存在しなかった場合の処理
            if (!dataFoundForSelectedVenue) {
                Toast.makeText(getContext(), "選択された競馬場のレースデータが見つかりませんでした。", Toast.LENGTH_LONG).show();
                buttonAnalyze.setEnabled(true);
                buttonAnalyzeRace.setEnabled(true);
                return;
            }

            // 6. AIへの指示書（プロンプト）を作成してAPIを呼び出す
            String prompt = "あなたは非常に優秀な競馬データアナリストです。以下の詳細なレース結果データを分析し、データ全体から読み取れる勝利馬の興味深い傾向や、騎手と人気の関係、あるいは何か特筆すべきパターンがあれば、プロの視点で詳しく解説してください。\n\n"
                    + raceDataText;

            callGeminiApiWithPrompt(prompt);

        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "レース結果の読み込みに失敗しました。", Toast.LENGTH_SHORT).show();
            buttonAnalyze.setEnabled(true);
            buttonAnalyzeRace.setEnabled(true);
        });
    }

    /**
     * カスタムプロンプトでGemini APIを呼び出すメソッド
     *
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
                                ChatMessage botMessage = new ChatMessage("【分析結果】\n" + botResponseText, "assistant");
                                chatDatabaseRef.push().setValue(botMessage);
                            } catch (Exception e) {
                                Log.e(TAG, "Response parsing error", e);
                                ChatMessage errorMessage = new ChatMessage("応答の解析に失敗しました。", "assistant");
                                chatDatabaseRef.push().setValue(errorMessage);
                            }
                        } else {
                            Log.e(TAG, "API Error: " + response.code() + " " + response.message());
                            ChatMessage errorMessage = new ChatMessage("申し訳ありません、分析中にAPIエラーが発生しました。(" + response.code() + ")", "assistant");
                            chatDatabaseRef.push().setValue(errorMessage);
                        }
                        // ★ 両方のボタンを再度有効化
                        buttonAnalyze.setEnabled(true);
                        buttonAnalyzeRace.setEnabled(true);
                    }

                    @Override
                    public void onFailure(@NonNull Call<GeminiChatResponse> call, @NonNull Throwable t) {
                        Log.e(TAG, "API Failure", t);
                        ChatMessage errorMessage = new ChatMessage("分析中に通信エラーが発生しました。", "assistant");
                        chatDatabaseRef.push().setValue(errorMessage);
                        // ★ 両方のボタンを再度有効化
                        buttonAnalyze.setEnabled(true);
                        buttonAnalyzeRace.setEnabled(true);
                    }
                });
    }

    /**
     * 通常のチャットメッセージを送信するメソッド
     */
    private void sendMessage() {
        // ★ chatDatabaseRefがnull（未ログイン）の場合は送信しない
        if (chatDatabaseRef == null) {
            Toast.makeText(getContext(), "ログインしてください", Toast.LENGTH_SHORT).show();
            return;
        }

        // ★★★ 利用回数チェック ★★★
        if (!isApiUsageAllowed()) {
            Toast.makeText(getContext(), "本日の分析機能の利用上限（" + DAILY_LIMIT + "回）に達しました。", Toast.LENGTH_LONG).show();
            return; // 処理を中断
        }
        // ★★★ 利用回数をインクリメント ★★★
        incrementApiUsageCount();

        String messageText = editTextMessage.getText().toString().trim();
        if (!TextUtils.isEmpty(messageText)) {
            ChatMessage userMessage = new ChatMessage(messageText, "user");
            chatDatabaseRef.push().setValue(userMessage);
            callGeminiApi(messageText);
            editTextMessage.setText("");
        }
    }

    /**
     * 通常のチャットメッセージでGoogle Gemini APIを呼び出すメソッド
     *
     * @param userMessageText ユーザーが入力したメッセージ
     */
    private void callGeminiApi(String userMessageText) {
        buttonSend.setEnabled(false);

        GeminiApiService apiService = ApiClient.getClient().create(GeminiApiService.class);

        GeminiChatRequest.Part part = new GeminiChatRequest.Part(userMessageText);
        GeminiChatRequest.Content content = new GeminiChatRequest.Content(Collections.singletonList(part));
        GeminiChatRequest request = new GeminiChatRequest(Collections.singletonList(content));

        apiService.generateContent("gemini-2.0-flash", BuildConfig.GEMINI_API_KEY, request)
                .enqueue(new Callback<GeminiChatResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<GeminiChatResponse> call, @NonNull Response<GeminiChatResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                String botResponseText = response.body().getCandidates().get(0).getContent().getParts().get(0).getText();
                                ChatMessage botMessage = new ChatMessage(botResponseText, "model");
                                chatDatabaseRef.push().setValue(botMessage);
                            } catch (Exception e) {
                                Log.e(TAG, "Response parsing error", e);
                                ChatMessage errorMessage = new ChatMessage("応答の解析に失敗しました。", "model");
                                chatDatabaseRef.push().setValue(errorMessage);
                            }
                        } else {
                            Log.e(TAG, "API Error: " + response.code() + " " + response.message());
                            ChatMessage errorMessage = new ChatMessage("申し訳ありません、APIエラーが発生しました。(" + response.code() + ")", "model");
                            chatDatabaseRef.push().setValue(errorMessage);
                        }
                        buttonSend.setEnabled(true);
                    }

                    @Override
                    public void onFailure(@NonNull Call<GeminiChatResponse> call, @NonNull Throwable t) {
                        Log.e(TAG, "API Failure", t);
                        ChatMessage errorMessage = new ChatMessage("通信に失敗しました。ネットワークを確認してください。", "model");
                        chatDatabaseRef.push().setValue(errorMessage);
                        buttonSend.setEnabled(true);
                    }
                });
    }

    /**
     * Firebaseのメッセージ変更を監視するリスナー
     */
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
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

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

    /**
     * ★★★ ユーザーIDを使ってデータベース参照を初期化し、リスナーを設定するメソッド ★★★
     */
    private void initializeDatabaseForUser() {
        if (currentUserId == null) {
            Log.e(TAG, "Cannot initialize database references because UID is null.");
            return;
        }

        // データベースのパスにユーザーIDを組み込む！
        chatDatabaseRef = FirebaseDatabase.getInstance()
                .getReference("chats") // ルートは "chats"
                .child(currentUserId)  // ★★★ ユーザーごとの一意なパス ★★★
                .child("messages");   // その下の "messages"

        // 既存のレース結果DB参照 (これは全ユーザー共通)
        raceResultDatabaseRef = FirebaseDatabase.getInstance().getReference("raceResult");

        // メッセージを監視するリスナーを開始
        listenForMessages();
    }

    /**
     * ★★★ データベースリスナーをクリーンアップするメソッド ★★★
     */
    private void cleanupDatabaseListener() {
        if (chatDatabaseRef != null && chatEventListener != null) {
            chatDatabaseRef.removeEventListener(chatEventListener);
            Log.d(TAG, "Previous chat listener removed.");
        }
    }

    // ★★★ Adapter.OnShareButtonClickListener の実装 ★★★
    @Override
    public void onShareClick(String text) {
        // 画像を生成して共有する
        Bitmap shareBitmap = createBitmapFromLayout(text);
        if (shareBitmap != null) {
            share(shareBitmap);
        } else {
            Toast.makeText(getContext(), "画像の生成に失敗しました", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 【新規追加】レイアウトからBitmap画像を生成する
     */
    private Bitmap createBitmapFromLayout(String contentText) {
        // 共有用レイアウトをインフレート
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.layout_share_image, null, false);

        // テキストをセット
        TextView contentTextView = view.findViewById(R.id.textViewShareContent);
        contentTextView.setText(contentText);

        // Viewのサイズを測定
        view.measure(
                View.MeasureSpec.makeMeasureSpec(360 * (int) getResources().getDisplayMetrics().density, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        // レイアウトを配置
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        // Bitmapを作成し、Canvasに描画
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    /**
     * 【新規追加】Bitmapを共有するメソッド
     */
    private void share(Bitmap bitmap) {
        // 1. Bitmapをファイルとしてキャッシュに保存
        File cachePath = new File(getContext().getCacheDir(), "images");
        cachePath.mkdirs();
        File imageFile = new File(cachePath, "share_image.png");
        try (FileOutputStream stream = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // 2. FileProviderを使ってファイルURIを取得 (重要)
        Uri contentUri = FileProvider.getUriForFile(getContext(),
                BuildConfig.APPLICATION_ID + ".provider", imageFile);

        if (contentUri == null) return;

        // 3. 共有インテントを作成
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/png");
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "AIの分析結果をチェック！ #AI競馬 #競馬予想　#tktkcompany https://play.google.com/store/apps/details?id=com.tktkcompany.kakoRaceKeiba"); // Twitterなどに投稿されるテキスト
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // 4. 共有ダイアログを表示
        startActivity(Intent.createChooser(shareIntent, "分析結果を共有"));
    }

    // ▼▼▼▼▼【ここから下のメソッドをすべて新規追加】▼▼▼▼▼

    /**
     * 【新規追加】APIの利用が許可されているかチェックするメソッド
     *
     * @return true if allowed, false otherwise
     */
    private boolean isApiUsageAllowed() {
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // 今日の日付を "yyyy-MM-dd" 形式の文字列で取得
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // 保存されている最終利用日を取得
        String lastUsageDate = prefs.getString(KEY_LAST_USAGE_DATE, "");

        int usageCount;

        if (todayDate.equals(lastUsageDate)) {
            // 最後に使ったのが今日なら、カウンターをそのまま使う
            usageCount = prefs.getInt(KEY_USAGE_COUNT, 0);
        } else {
            // 最後に使ったのが昨日以前なら、カウンターをリセット
            usageCount = 0;
            // SharedPreferencesをクリアしてリセットする
            resetApiUsageCount(todayDate);
        }

        // 利用回数が上限未満かチェック
        return usageCount < DAILY_LIMIT;
    }

    /**
     * 【新規追加】APIの利用回数を1増やすメソッド
     */
    private void incrementApiUsageCount() {
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // 現在のカウントを取得（日付が変わっていれば0になっているはず）
        int currentCount = prefs.getInt(KEY_USAGE_COUNT, 0);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_USAGE_COUNT, currentCount + 1);
        editor.putString(KEY_LAST_USAGE_DATE, todayDate); // 最終利用日も今日に更新
        editor.apply(); // 保存
    }

    /**
     * 【新規追加】APIの利用回数をリセットするメソッド
     */
    private void resetApiUsageCount(String todayDate) {
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_USAGE_COUNT, 0); // カウンターを0に
        editor.putString(KEY_LAST_USAGE_DATE, todayDate); // 最終利用日を今日に
        editor.apply();
    }

}