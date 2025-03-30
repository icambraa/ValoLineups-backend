package com.valolineups.backend.services;

import com.valolineups.backend.models.Lineup;
import com.valolineups.backend.repositories.LineupRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Service
public class LineupService {
    private final LineupRepository lineupRepository;

    public LineupService(LineupRepository lineupRepository) {
        this.lineupRepository = lineupRepository;
    }

    public Lineup createLineup(Lineup lineup) {

        String generatedTitle = generateTitle(lineup);
        lineup.setTitle(generatedTitle);
        return lineupRepository.save(lineup);
    }

    public List<Lineup> getAllLineups() {
        return lineupRepository.findAll();
    }

    private String generateTitle(Lineup lineup) {
        return lineup.getAgent() + " usando " + lineup.getAbilities() +
                " desde " + lineup.getExecutedOn() +
                " hacia " + lineup.getAffectedArea();
    }

    public List<Lineup> getUserLineupsPaginated(String uploadedBy, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return lineupRepository.findByUploadedByOrderByUploadDateDesc(uploadedBy, pageable).getContent();

    }

    public Optional<Lineup> getLineupById(Long id) {
        return lineupRepository.findById(id);
    }

}
