package com.example.lolapp.service;

import com.example.lolapp.dto.AptitudeAnswerDto;
import com.example.lolapp.dto.AptitudeRequestDto;
import com.example.lolapp.dto.ChampionDto;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AptitudeTestService {

    private final ChampionService championService;

    public AptitudeTestService(ChampionService championService) {
        this.championService = championService;
    }

    public List<ChampionDto> calculateTopChampions(AptitudeRequestDto request) {
        List<ChampionDto> allChamps = championService.getAllChampions();
        Map<Long, Double> scores = new HashMap<>();
        Map<Long, Double> randomTieBreakers = new HashMap<>();
        Random rand = new Random();

        for (ChampionDto champ : allChamps) {
            double score = 0.0;
            randomTieBreakers.put(champ.getId(), rand.nextDouble());

            for (AptitudeAnswerDto ans : request.getAnswers()) {
                if ("any".equals(ans.getType()) || ans.getValues() == null || ans.getValues().isEmpty()) {
                    continue;
                }

                boolean isMatch = false;
                String type = ans.getType();
                List<String> values = ans.getValues();

                if ("lanes".equals(type) && champ.getLanes() != null) {
                    isMatch = champ.getLanes().stream().anyMatch(values::contains);
                } else if ("rangeType".equals(type) && champ.getRangeType() != null) {
                    isMatch = values.contains(champ.getRangeType());
                } else if ("roles".equals(type) && champ.getRoles() != null) {
                    isMatch = champ.getRoles().stream().anyMatch(values::contains);
                } else if ("damagePrimary".equals(type) && champ.getDamagePrimary() != null) {
                    isMatch = values.contains(champ.getDamagePrimary());
                } else if ("difficulty".equals(type)) {
                    isMatch = values.contains(String.valueOf(champ.getDifficulty()));
                } else if ("visuals".equals(type) || "visuals_multi".equals(type)) {
                    if (champ.getVisuals() != null) {
                        isMatch = champ.getVisuals().stream().anyMatch(values::contains);
                    }
                }

                if (isMatch) {
                    // ビジュアル・雰囲気系の質問は配点を高くする
                    double points = type.startsWith("visuals") ? 1.5 : 1.0;
                    score += points;
                }
            }
            scores.put(champ.getId(), score);
        }

        // スコア順にソートし、同点の場合はランダム値でタイブレークして上位3名を返す
        return allChamps.stream()
                .sorted((c1, c2) -> {
                    int scoreCompare = Double.compare(scores.get(c2.getId()), scores.get(c1.getId()));
                    if (scoreCompare != 0) {
                        return scoreCompare;
                    }
                    return Double.compare(randomTieBreakers.get(c2.getId()), randomTieBreakers.get(c1.getId()));
                })
                .limit(3)
                .collect(Collectors.toList());
    }
}