package com.tktkcompany.kakoRaceKeiba.ui.home;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tktkcompany.kakoRaceKeiba.R;

import java.util.List;

public class HorizontalAdapter extends RecyclerView.Adapter<HorizontalAdapter.ViewHolder> {
    private List<String> mData;
    private Context mContext;

    public HorizontalAdapter(Context context, List<String> data) {
        this.mContext = context;
        this.mData = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String item = mData.get(position);
        // ボタンにテキストを設定
        holder.button.setText(item);
        // アイテムに対応するデータを設定
        if(item.equals("本日の馬場状態")) {
            holder.button.setOnClickListener(v -> {
                // ボタンがクリックされたときに外部サイトにアクセス
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.jra.go.jp/keiba/baba/index.html"));
                mContext.startActivity(intent);
            });
        }

        if(item.equals("レースカレンダー")) {
            holder.button.setOnClickListener(v -> {
                // ボタンがクリックされたときに外部サイトにアクセス
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.jra.go.jp/keiba/calendar/"));
                mContext.startActivity(intent);
            });
        }

        if(item.equals("JRAのYoutubeサイト")) {
            holder.button.setOnClickListener(v -> {
                // ボタンがクリックされたときに外部サイトにアクセス
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/channel/UCj6AKkCWS6FJqf0o5wP45eQ"));
                mContext.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        Button button;

        public ViewHolder(View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.button);
        }
    }
}
