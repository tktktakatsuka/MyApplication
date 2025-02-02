package com.tktkcompany.kakoRaceKeiba.ui.memo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.tktkcompany.kakoRaceKeiba.R;
import com.tktkcompany.kakoRaceKeiba.db.MyDatabaseManager;

import java.util.List;

public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.MemoViewHolder> {

    private final List<Memo> memoList;
    private final OnMemoClickListener listener;
    private final MyDatabaseManager databaseManager;

    public interface OnMemoClickListener {
        void onMemoClick(Memo memo);
    }

    public MemoAdapter(List<Memo> memoList, MyDatabaseManager databaseManager, OnMemoClickListener listener) {
        this.memoList = memoList;
        this.databaseManager = databaseManager;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MemoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_memo, parent, false);
        return new MemoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemoViewHolder holder, int position) {
        Memo memo = memoList.get(position);
        holder.bind(memo, listener, position);
    }

    @Override
    public int getItemCount() {
        return memoList.size();
    }

    class MemoViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleView;
        private final Button deleteButton;
        private Memo memo;

        MemoViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.text_view_title2);
            deleteButton = itemView.findViewById(R.id.button_delete_memo);

            titleView.setOnClickListener(v -> {
                if (memo != null) {
                    NavController navController = Navigation.findNavController(v);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("memo", memo);
                    navController.navigate(R.id.action_navigation_memoLists_to_navigation_memoDetail, bundle);
                }
            });

            // 削除ボタンのクリックリスナー
            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    databaseManager.deleteMemo(memo.getTitle()); // DBから削除
                    memoList.remove(position); // リストから削除
                    notifyItemRemoved(position); // UI更新
                }
            });
        }

        void bind(Memo memo, OnMemoClickListener listener, int position) {
            this.memo = memo;
            titleView.setText(memo.getTitle());
            itemView.setOnClickListener(v -> listener.onMemoClick(memo));
        }
    }
}
