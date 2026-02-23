package com.example.lolapp.dto;

import com.example.lolapp.model.Champion;
import lombok.Data;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ChampionDto {
    private Long id;
    private String champKey;
    private String name;
    private String title;
    private Integer difficulty;
    private String rangeType;
    private String damagePrimary;
    private String resourceType;
    private String imagePath;
    private String splashPath;
    private String loadingPath;
    private String tilePath;

    private List<String> roles;
    private List<String> lanes;
    private List<String> regions;
    private List<String> visuals;
    private List<SpellDto> spells;

    @Data
    public static class SpellDto {
        private String slot;
        private String name;
        private String description;
        private String imagePath;
    }

    public static ChampionDto fromEntity(Champion entity) {
        ChampionDto dto = new ChampionDto();
        dto.setId(entity.getId());
        dto.setChampKey(entity.getChampKey());
        dto.setName(entity.getName());
        dto.setTitle(entity.getTitle());
        dto.setDifficulty(entity.getDifficulty());
        dto.setRangeType(entity.getRangeType());
        dto.setDamagePrimary(entity.getDamagePrimary());
        dto.setResourceType(entity.getResourceType());
        dto.setImagePath(entity.getImagePath());
        dto.setSplashPath(entity.getSplashPath());
        dto.setLoadingPath(entity.getLoadingPath());
        dto.setTilePath(entity.getTilePath());

        dto.setRoles(entity.getRoles().stream().map(r -> r.getRoleName()).collect(Collectors.toList()));
        dto.setLanes(entity.getLanes().stream().map(l -> l.getLaneName()).collect(Collectors.toList()));
        dto.setRegions(entity.getRegions().stream().map(r -> r.getRegionName()).collect(Collectors.toList()));
        dto.setVisuals(entity.getVisuals().stream().map(v -> v.getVisualName()).collect(Collectors.toList()));

        dto.setSpells(entity.getSpells().stream().map(s -> {
            SpellDto sdto = new SpellDto();
            sdto.setSlot(s.getSlot());
            sdto.setName(s.getName());
            sdto.setDescription(s.getDescription());
            sdto.setImagePath(s.getImagePath());
            return sdto;
        }).collect(Collectors.toList()));

        return dto;
    }
}