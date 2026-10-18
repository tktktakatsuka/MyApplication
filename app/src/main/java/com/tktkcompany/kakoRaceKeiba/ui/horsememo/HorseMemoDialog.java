package com.tktkcompany.kakoRaceKeiba.ui.horsememo;

import com.tktkcompany.kakoRaceKeiba.util.Lang;

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
            Toast.makeText(context, Lang.t("メモ機能を利用するにはログインが必要です", "Sign in to use notes"), Toast.LENGTH_SHORT).show();
            return;
        }
        HorseMemos.Memo existing = HorseMemos.find(memos, horseName);

        float density = context.getResources().getDisplayMetrics().density;
        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setMinLines(3);
        input.setGravity(Gravity.TOP | Gravity.START);
        input.setHint(Lang.t("例: 4角で前が詰まって脚を余した。次走は買い", "e.g. Got blocked at the turn, had more to give. Back next time"));
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
                .setMessage(Lang.t("メモした馬が出走すると、出走表とホームでお知らせします", "When this horse runs, you'll see it on the race card and Home"))
                .setView(box)
                .setPositiveButton(Lang.t("保存", "Save"), (d, w) -> {
                    String text = input.getText().toString();
                    HorseMemos.Memo saved = HorseMemos.save(memos, horseName, text,
                            from != null ? from : existing != null ? existing.from : null);
                    if (saved == null && existing == null) return; // 空のまま保存した
                    Toast.makeText(context, saved != null ? Lang.t("メモを保存しました", "Note saved") : Lang.t("メモを削除しました", "Note deleted"), Toast.LENGTH_SHORT).show();
                    onChanged.onChanged(saved);
                })
                .setNegativeButton(Lang.t("キャンセル", "Cancel"), null);
        if (existing != null) {
            builder.setNeutralButton(Lang.t("削除", "Delete"), (d, w) -> {
                HorseMemos.delete(memos, horseName);
                Toast.makeText(context, Lang.t("メモを削除しました", "Note deleted"), Toast.LENGTH_SHORT).show();
                onChanged.onChanged(null);
            });
        }
        builder.show();
    }
}
