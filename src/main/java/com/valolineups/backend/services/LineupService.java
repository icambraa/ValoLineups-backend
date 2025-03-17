package com.valolineups.backend.services;

import com.valolineups.backend.models.Lineup;
import com.valolineups.backend.repositories.LineupRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
