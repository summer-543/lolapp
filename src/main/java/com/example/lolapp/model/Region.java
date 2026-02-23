package com.example.lolapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "regions")
public class Region {
    @Id
    private Long id;
    @Column(unique = true)
    private String regionName;
}