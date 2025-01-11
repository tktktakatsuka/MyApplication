package com.tktkcompany.kakoRaceKeiba.ui.notifications;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TabFragment extends Fragment {

    private static final String ARG_TAB_TITLE = "tab_title";

    public static TabFragment newInstance(String tabTitle) {
        TabFragment fragment = new TabFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TAB_TITLE, tabTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(android.R.layout.simple_list_item_1, container, false);
        TextView textView = view.findViewById(android.R.id.text1);

        if (getArguments() != null) {
            String tabTitle = getArguments().getString(ARG_TAB_TITLE);
            textView.setText(tabTitle);
        }

        return view;
    }
}
