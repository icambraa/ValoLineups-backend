package com.valolineups.backend.repositories;

import com.valolineups.backend.models.LineupImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LineupImageRepository extends JpaRepository<LineupImage, Long> {
}
