package com.tktkcompany.kakoRaceKeiba.dto;

import java.util.List;

public class GeminiChatResponse {
    private List<Candidate> candidates;

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public static class Candidate {
        private Content content;

        public Content getContent() {
            return content;
        }
    }

    public static class Content {
        private List<Part> parts;
        private String role;

        public List<Part> getParts() {
            return parts;
        }

        public String getRole() {
            return role;
        }
    }

    public static class Part {
        private String text;

        public String getText() {
            return text;
        }
    }

}