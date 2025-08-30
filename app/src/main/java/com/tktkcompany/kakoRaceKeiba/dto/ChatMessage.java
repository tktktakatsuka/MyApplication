package com.tktkcompany.kakoRaceKeiba.dto;


public class ChatMessage {
    private String text;
    private String role; // "user" または "assistant"
    private long timestamp;

    // Firebaseがデータを読み書きするために空のコンストラクタが必要
    public ChatMessage() {
    }

    public ChatMessage(String text, String role) {
        this.text = text;
        this.role = role;
        this.timestamp = System.currentTimeMillis();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}