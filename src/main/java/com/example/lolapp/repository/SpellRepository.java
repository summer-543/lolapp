package com.example.lolapp.repository;

import com.example.lolapp.model.Spell;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpellRepository extends JpaRepository<Spell, Long> {
}