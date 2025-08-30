package com.tktkcompany.kakoRaceKeiba.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.tktkcompany.kakoRaceKeiba.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class WebViewDialogFragment extends DialogFragment {

    private static final String ARG_URL = "url";
    private String urlToLoad;

    public static WebViewDialogFragment newInstance(String url) {
        WebViewDialogFragment fragment = new WebViewDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            urlToLoad = getArguments().getString(ARG_URL);
        }
        // オプション: ダイアログのスタイルをカスタマイズ (例: フルスクリーンに近い形)
        // setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_webview_dialog, container, false); // XMLレイアウトファイル
        WebView webView = view.findViewById(R.id.webViewInDialog);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                // APIレベル 23以上
                if (getContext() != null && error != null) {
                    // Toast.makeText(getContext(), "Error loading page: " + error.getDescription(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                // APIレベル 23未満
                if (getContext() != null) {
                    // Toast.makeText(getContext(), "Error loading page: " + description, Toast.LENGTH_LONG).show();
                }
            }
        });

        webView.getSettings().setJavaScriptEnabled(true); // JavaScriptを有効にする
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false); // ズームボタンは非表示




        if (urlToLoad != null && !urlToLoad.isEmpty()) {
            webView.loadUrl(urlToLoad);
        } else {
            if (getContext() != null) {
                Toast.makeText(getContext(), "URL not provided", Toast.LENGTH_SHORT).show();
            }
            dismiss(); // URLがない場合はダイアログを閉じる
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT; // または特定の高さ
            if (dialog.getWindow() != null) {
                dialog.getWindow().setLayout(width, height);
                // dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // 背景を透明にする場合
            }
        }
    }
}