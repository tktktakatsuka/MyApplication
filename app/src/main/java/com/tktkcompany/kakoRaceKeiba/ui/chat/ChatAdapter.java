// ファイル名: ChatAdapter.java
package com.tktkcompany.kakoRaceKeiba.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tktkcompany.kakoRaceKeiba.R;
import com.tktkcompany.kakoRaceKeiba.dto.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    // --- 1. リスナー用のインターフェース定義 ---
    public interface OnShareButtonClickListener {
        void onShareClick(String text);
    }

    // --- 2. フィールド変数 ---
    private final List<ChatMessage> messageList;
    private OnShareButtonClickListener shareButtonClickListener; // ★★★ 不足していたリスナー変数を定義

    // --- 3. 定数 ---
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_BOT = 2; // ★★★ "VIEW_TYPE_OPPONENT" を "VIEW_TYPE_BOT" に統一

    // --- 4. コンストラクタ ---
    public ChatAdapter(List<ChatMessage> messageList) {
        this.messageList = messageList;
    }

    // --- 5. リスナーをセットするためのメソッド ---
    public void setOnShareButtonClickListener(OnShareButtonClickListener listener) {
        this.shareButtonClickListener = listener;
    }

    // --- 6. RecyclerView.Adapterの必須メソッド ---
    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messageList.get(position);
        if ("user".equals(message.getRole())) {
            return VIEW_TYPE_USER;
        } else {
            return VIEW_TYPE_BOT;
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_USER) {
            // ユーザー用のレイアウトをインフレート
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
        } else {
            // ボット（相手）用のレイアウトをインフレート
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_bot, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        holder.textViewMessage.setText(message.getText());

        ImageButton shareButton = holder.itemView.findViewById(R.id.buttonShare);

        // getItemViewType() の結果を使って判定
        if (getItemViewType(position) == VIEW_TYPE_BOT) {
            // ボット（相手）のメッセージの場合
            if (shareButton != null) {
                shareButton.setVisibility(View.VISIBLE);
                shareButton.setOnClickListener(v -> {
                    // ★ リスナーがセットされていれば、クリックイベントを通知
                    if (shareButtonClickListener != null) {
                        shareButtonClickListener.onShareClick(message.getText());
                    }
                });
            }
        } else {
            // ユーザーのメッセージの場合
            if (shareButton != null) {
                // 共有ボタンはユーザーのメッセージにはないので、非表示にする
                shareButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // --- 7. ViewHolder ---
    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMessage;
        // ImageButton shareButton; // ここで宣言する必要はない

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            // shareButton = itemView.findViewById(R.id.buttonShare); // onBindViewHolderで取得するのでここでは不要
        }
    }
}