package com.example.lolapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "champions")
public class Champion {
    @Id
    private Long id;
    @Column(unique = true)
    private String champKey;
    @Column(nullable = false)
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

    // 1対多 (スキル)
    @OneToMany(mappedBy = "champion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Spell> spells = new ArrayList<>();

    // 以下、多対多 (マスタ系の中間テーブル自動生成)
    @ManyToMany
    @JoinTable(name = "champion_roles", joinColumns = @JoinColumn(name = "champion_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "champion_lanes", joinColumns = @JoinColumn(name = "champion_id"), inverseJoinColumns = @JoinColumn(name = "lane_id"))
    private List<Lane> lanes = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "champion_regions", joinColumns = @JoinColumn(name = "champion_id"), inverseJoinColumns = @JoinColumn(name = "region_id"))
    private List<Region> regions = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "champion_visuals", joinColumns = @JoinColumn(name = "champion_id"), inverseJoinColumns = @JoinColumn(name = "visual_id"))
    private List<Visual> visuals = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "champion_combat_styles", joinColumns = @JoinColumn(name = "champion_id"), inverseJoinColumns = @JoinColumn(name = "style_id"))
    private List<CombatStyle> combatStyles = new ArrayList<>();
}