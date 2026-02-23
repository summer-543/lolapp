package com.example.lolapp.controller;

import com.example.lolapp.dto.ChampionDto;
import com.example.lolapp.service.ChampionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/champions")
public class ChampionController {

    private final ChampionService championService;

    public ChampionController(ChampionService championService) {
        this.championService = championService;
    }

    // 全件取得API: http://localhost:8080/api/champions
    @GetMapping
    public List<ChampionDto> getAll() {
        return championService.getAllChampions();
    }

    // ID指定取得API: http://localhost:8080/api/champions/2 (例: アーリ)
    @GetMapping("/{id}")
    public ChampionDto getById(@PathVariable Long id) {
        return championService.getChampionById(id);
    }
}