package com.example.lolapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "combat_styles")
public class CombatStyle {
    @Id
    private Long id;
    @Column(unique = true)
    private String styleName;
}