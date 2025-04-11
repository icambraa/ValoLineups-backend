package com.valolineups.backend.repositories;

import com.valolineups.backend.models.Lineup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.valolineups.backend.models.User;

@Repository
public interface LineupRepository extends JpaRepository<Lineup, Long> {

    // Page<Lineup> findByUploadedBy(String uploadedBy, Pageable pageable);

    Page<Lineup> findByUploadedByOrderByUploadDateDesc(User uploadedBy, Pageable pageable);

    Page<Lineup> findByIsGeneralTrueOrderByUploadDateDesc(Pageable pageable);

    Page<Lineup> findByPendingReviewTrueOrderByUploadDateDesc(Pageable pageable);

    Page<Lineup> findByUploadedByAndIsGeneralTrueOrderByUploadDateDesc(User uploadedBy, Pageable pageable);

}
