package com.example.lolapp.repository;

import com.example.lolapp.model.Champion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChampionRepository extends JpaRepository<Champion, Long> {
}