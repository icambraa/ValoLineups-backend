package com.valolineups.backend.services;

import com.valolineups.backend.models.Lineup;
import com.valolineups.backend.models.LineupImage;
import com.valolineups.backend.repositories.LineupRepository;
import com.valolineups.backend.repositories.LineupImageRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LineupService {

    private final LineupRepository lineupRepository;
    private final LineupImageRepository lineupImageRepository;

    public LineupService(LineupRepository lineupRepository, LineupImageRepository lineupImageRepository) {
        this.lineupRepository = lineupRepository;
        this.lineupImageRepository = lineupImageRepository;
    }

    public Lineup createLineupWithImages(Lineup lineup, List<String> imageUrls) {
        String generatedTitle = generateTitle(lineup);
        lineup.setTitle(generatedTitle);

        Lineup savedLineup = lineupRepository.save(lineup);

        if (imageUrls != null && !imageUrls.isEmpty()) {
            List<LineupImage> images = imageUrls.stream().map(url -> {
                LineupImage image = new LineupImage();
                image.setUrl(url);
                image.setLineup(savedLineup);
                return image;
            }).collect(Collectors.toList());

            lineupImageRepository.saveAll(images);

            savedLineup.setImages(images);
        }

        return savedLineup;
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
