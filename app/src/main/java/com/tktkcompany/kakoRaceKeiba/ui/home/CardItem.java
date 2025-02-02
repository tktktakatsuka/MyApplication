package com.tktkcompany.kakoRaceKeiba.ui.home;
public class CardItem {
    private final String title;
    private final String subtitle;
    private final int imageResId;

    public CardItem(String title, String subtitle, int imageResId) {
        this.title = title;
        this.subtitle = subtitle;
        this.imageResId = imageResId;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public int getImageResId() {
        return imageResId;
    }
}
