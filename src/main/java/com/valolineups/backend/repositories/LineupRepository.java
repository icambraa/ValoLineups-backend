package com.valolineups.backend.repositories;

import com.valolineups.backend.models.Lineup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface LineupRepository extends JpaRepository<Lineup, Long> {

    Page<Lineup> findByUploadedBy(String uploadedBy, Pageable pageable);

    Page<Lineup> findByUploadedByOrderByUploadDateDesc(String uploadedBy, Pageable pageable);

}
