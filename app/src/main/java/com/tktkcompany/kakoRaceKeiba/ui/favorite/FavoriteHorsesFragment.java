package com.tktkcompany.kakoRaceKeiba.ui.favorite;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.tktkcompany.kakoRaceKeiba.R;
import com.tktkcompany.kakoRaceKeiba.db.FirebaseManager;

import java.util.ArrayList;
import java.util.List;

public class FavoriteHorsesFragment extends Fragment {

    private RecyclerView recyclerView;
    private FavoriteHorsesAdapter adapter;
    private ProgressBar progressBar;
    private TextView noDataText;
    private List<String> favoriteHorses = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_favorite_horses, container, false);

        recyclerView = root.findViewById(R.id.recycler_view_favorites);
        progressBar = root.findViewById(R.id.progress_bar);
        noDataText = root.findViewById(R.id.text_no_data);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FavoriteHorsesAdapter(favoriteHorses, this::onRemoveClick);
        recyclerView.setAdapter(adapter);

        // 戻るボタンの設定
        root.findViewById(R.id.btn_back).setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigateUp(); // または特定のアクションを指定
        });

        loadFavorites();

        return root;
    }

    private void loadFavorites() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            noDataText.setText("ログインするとお気に入り機能を利用できます");
            noDataText.setVisibility(View.VISIBLE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        noDataText.setVisibility(View.GONE);
        
        // addValueEventListener に変更してリアルタイム更新を有効にする
        FirebaseManager.getFavoriteHorses(user.getUid(), new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                
                favoriteHorses.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String name = child.getKey();
                    if (name != null) {
                        favoriteHorses.add(name);
                    }
                }
                
                progressBar.setVisibility(View.GONE);
                if (favoriteHorses.isEmpty()) {
                    noDataText.setVisibility(View.VISIBLE);
                    noDataText.setText("お気に入りに登録された馬はいません");
                } else {
                    noDataText.setVisibility(View.GONE);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) {
                    progressBar.setVisibility(View.GONE);
                    Log.e("FavoriteHorses", "Load failed: " + error.getMessage());
                }
            }
        });
    }

    private void onRemoveClick(String horseName) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // データベースから削除。ValueEventListener が自動的にUIを更新するはずだが、
            // ここでは即時性を高めるために手動でも消しておく
            FirebaseManager.removeFavoriteHorse(user.getUid(), horseName);
            Toast.makeText(getContext(), horseName + "を削除しました", Toast.LENGTH_SHORT).show();
        }
    }
}
