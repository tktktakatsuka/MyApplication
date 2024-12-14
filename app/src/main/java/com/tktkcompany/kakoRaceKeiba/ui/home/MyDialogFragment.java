package com.tktkcompany.kakoRaceKeiba.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.tktkcompany.kakoRaceKeiba.R;

public class MyDialogFragment extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // ダイアログのレイアウトをインフレート
        View view = inflater.inflate(R.layout.dialog_layout, container, false);

        // UI要素の参照を取得
        TextView messageTextView = view.findViewById(R.id.message);
        return view;
    }
}
