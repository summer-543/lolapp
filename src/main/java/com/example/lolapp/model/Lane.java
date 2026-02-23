package com.example.lolapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "lanes")
public class Lane {
    @Id
    private Long id;
    @Column(unique = true)
    private String laneName;
}