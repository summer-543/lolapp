package com.example.lolapp.repository;

import com.example.lolapp.model.Lane;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LaneRepository extends JpaRepository<Lane, Long> {
}