package com.example.lolapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "spells")
public class Spell {
    @Id
    private Long id;

    // JSON変換時の無限ループを防ぐため、逆方向の参照を無視
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "champion_id")
    private Champion champion;

    @Column(nullable = false)
    private String slot;
    @Column(nullable = false)
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String imagePath;
}