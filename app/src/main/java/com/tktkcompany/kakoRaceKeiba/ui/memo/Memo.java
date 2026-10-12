package com.tktkcompany.kakoRaceKeiba.ui.memo;

import java.io.Serializable;

public class Memo  implements Serializable {
    // 端末保存時はSQLiteのrowid、クラウド保存時はFirebaseのキー
    private String id;
    private String title;
    private String content;

    public Memo(String id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
