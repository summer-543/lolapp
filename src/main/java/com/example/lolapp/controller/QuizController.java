package com.example.lolapp.controller;

import com.example.lolapp.dto.QuizDto;
import com.example.lolapp.service.QuizService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping("/start")
    public QuizDto.StartResponse startQuiz(@RequestBody QuizDto.StartRequest request) {
        return quizService.startQuiz(request);
    }

    @GetMapping("/next")
    public QuizDto.QuestionDto getNextQuestion(@RequestParam String sessionId) {
        return quizService.getNextQuestion(sessionId);
    }

    @PostMapping("/check")
    public QuizDto.CheckResponse checkAnswer(@RequestBody QuizDto.CheckRequest request) {
        return quizService.checkAnswer(request);
    }
}