package com.valolineups.backend.repositories;

import com.valolineups.backend.models.Lineup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LineupRepository extends JpaRepository<Lineup, Long> {
}
