package com.tktkcompany.kakoRaceKeiba.ui.memo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tktkcompany.kakoRaceKeiba.R;

public class MemoCreateFragment extends Fragment {
    private MemoRepository memoRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_memo_create, container, false);

        EditText titleInput = view.findViewById(R.id.edit_text_title);
        EditText contentInput = view.findViewById(R.id.edit_text_content);
        Button saveButton = view.findViewById(R.id.button_save);

        saveButton.setOnClickListener(v -> {
            String title = titleInput.getText().toString();
            String content = contentInput.getText().toString();

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(getContext(), "タイトルと本文を入力してください", Toast.LENGTH_SHORT).show();
                return;
            }

            saveButton.setEnabled(false);
            memoRepository = new MemoRepository(requireContext());
            memoRepository.add(title, content, success -> {
                if (!isAdded()) return;
                saveButton.setEnabled(true);
                if (!success) {
                    Toast.makeText(getContext(), "メモの保存に失敗しました", Toast.LENGTH_SHORT).show();
                    return;
                }
                // メモ一覧に戻る
                NavController navController = Navigation.findNavController(v);
                navController.popBackStack();
            });
        });

        return view;
    }
}