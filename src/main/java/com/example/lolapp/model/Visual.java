package com.example.lolapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "visuals")
public class Visual {
    @Id
    private Long id;
    @Column(unique = true)
    private String visualName;
    private String category;
}