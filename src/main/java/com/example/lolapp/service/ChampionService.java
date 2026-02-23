package com.example.lolapp.service;

import com.example.lolapp.dto.ChampionDto;
import com.example.lolapp.model.Champion;
import com.example.lolapp.repository.ChampionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChampionService {

    private final ChampionRepository championRepository;

    public ChampionService(ChampionRepository championRepository) {
        this.championRepository = championRepository;
    }

    // 全チャンピオンを取得し、DTOに変換して返す
    @Transactional(readOnly = true)
    public List<ChampionDto> getAllChampions() {
        return championRepository.findAll().stream()
                .map(ChampionDto::fromEntity)
                .collect(Collectors.toList());
    }

    // IDで特定のチャンピオンを検索し、DTOに変換して返す
    @Transactional(readOnly = true)
    public ChampionDto getChampionById(Long id) {
        Champion champion = championRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Champion not found with id: " + id));
        return ChampionDto.fromEntity(champion);
    }
}