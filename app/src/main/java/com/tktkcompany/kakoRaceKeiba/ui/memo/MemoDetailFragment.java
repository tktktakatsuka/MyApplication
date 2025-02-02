package com.tktkcompany.kakoRaceKeiba.ui.memo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.tktkcompany.kakoRaceKeiba.R;

public class MemoDetailFragment extends Fragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_CONTENT = "content";
    private Memo memo;
    public static MemoDetailFragment newInstance(Memo memo) {
        MemoDetailFragment fragment = new MemoDetailFragment();

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_memo_detail, container, false);

        TextView titleView = view.findViewById(R.id.text_view_title);
        TextView contentView = view.findViewById(R.id.text_view_content);
        Button editButton = view.findViewById(R.id.button_edit_memo);

        if (getArguments() != null) {
            memo = (Memo) getArguments().getSerializable("memo");
            titleView.setText(memo.getTitle());
            contentView.setText(memo.getContent());
        }

        editButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("memo", memo);
            Navigation.findNavController(v).navigate(R.id.action_navigation_memoDetail_to_navigation_memoEdit, bundle);
        });

        return view;
    }
}

