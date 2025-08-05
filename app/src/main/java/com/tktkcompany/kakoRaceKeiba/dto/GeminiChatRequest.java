package com.tktkcompany.kakoRaceKeiba.dto;

import java.util.List;

public class GeminiChatRequest {
    private final List<Content> contents;

    public GeminiChatRequest(List<Content> contents) {
        this.contents = contents;
    }

    public static class Content {
        private final List<Part> parts;

        public Content(List<Part> parts) {
            this.parts = parts;
        }
    }

    public static class Part {
        private final String text;

        public Part(String text) {
            this.text = text;
        }
    }
}