package com.example.lolapp.controller;

import com.example.lolapp.dto.AptitudeRequestDto;
import com.example.lolapp.dto.ChampionDto;
import com.example.lolapp.service.AptitudeTestService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/aptitude-test")
public class AptitudeTestController {

    private final AptitudeTestService aptitudeTestService;

    public AptitudeTestController(AptitudeTestService aptitudeTestService) {
        this.aptitudeTestService = aptitudeTestService;
    }

    @PostMapping
    public List<ChampionDto> calculate(@RequestBody AptitudeRequestDto request) {
        return aptitudeTestService.calculateTopChampions(request);
    }
}