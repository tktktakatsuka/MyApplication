package com.tktkcompany.kakoRaceKeiba.ui.horsememo;

import android.content.Context;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.util.Map;

/** 馬メモを書く・直す・消すダイアログ */
public final class HorseMemoDialog {

    public interface OnChanged {
        /** 保存後のメモ。削除したときは null */
        void onChanged(@Nullable HorseMemos.Memo memo);
    }

    private HorseMemoDialog() {
    }

    /**
     * @param from 今どの場面から書いているか（例: 「10/12(月) 東京3R 5着」）。メモと一緒に残す
     */
    public static void show(Context context, Map<String, HorseMemos.Memo> memos, String horseName,
                            @Nullable String from, OnChanged onChanged) {
        if (!HorseMemos.isSignedIn()) {
            Toast.makeText(context, "メモ機能を利用するにはログインが必要です", Toast.LENGTH_SHORT).show();
            return;
        }
        HorseMemos.Memo existing = HorseMemos.find(memos, horseName);

        float density = context.getResources().getDisplayMetrics().density;
        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setMinLines(3);
        input.setGravity(Gravity.TOP | Gravity.START);
        input.setHint("例: 4角で前が詰まって脚を余した。次走は買い");
        if (existing != null) {
            input.setText(existing.text);
            input.setSelection(input.getText().length());
        }
        FrameLayout box = new FrameLayout(context);
        int pad = (int) (20 * density);
        box.setPadding(pad, (int) (8 * density), pad, 0);
        box.addView(input);

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("📝 " + HorseMemos.displayName(horseName))
                .setMessage("メモした馬が出走すると、出走表とホームでお知らせします")
                .setView(box)
                .setPositiveButton("保存", (d, w) -> {
                    String text = input.getText().toString();
                    HorseMemos.Memo saved = HorseMemos.save(memos, horseName, text,
                            from != null ? from : existing != null ? existing.from : null);
                    if (saved == null && existing == null) return; // 空のまま保存した
                    Toast.makeText(context, saved != null ? "メモを保存しました" : "メモを削除しました", Toast.LENGTH_SHORT).show();
                    onChanged.onChanged(saved);
                })
                .setNegativeButton("キャンセル", null);
        if (existing != null) {
            builder.setNeutralButton("削除", (d, w) -> {
                HorseMemos.delete(memos, horseName);
                Toast.makeText(context, "メモを削除しました", Toast.LENGTH_SHORT).show();
                onChanged.onChanged(null);
            });
        }
        builder.show();
    }
}
