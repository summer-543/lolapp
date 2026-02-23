package com.example.lolapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

public class QuizDto {

    @Data
    public static class StartRequest {
        private String genre; // ALL, VISUAL, SPELL, KNOWLEDGE
        private String difficulty; // NORMAL, HARD
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StartResponse {
        private String sessionId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuestionDto {
        private String questionId;
        private String text;
        private String imageUrl;
        private List<OptionDto> options;
        private boolean isSplash;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OptionDto {
        private String optionId;
        private String text;
        private String imageUrl;
    }

    @Data
    public static class CheckRequest {
        private String sessionId;
        private String questionId;
        private String selectedOptionId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CheckResponse {
        private boolean correct;
        private String correctOptionId;
        private int currentScore;
        private int currentStreak;
        private String explanation;
        private int remainingLives;
        private boolean gameOver;
    }
}