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

import com.valolineups.backend.models.User;
import com.valolineups.backend.repositories.UserRepository;

@Service
public class LineupService {

    private final LineupRepository lineupRepository;
    private final LineupImageRepository lineupImageRepository;
    private final UserRepository userRepository;

    public LineupService(
            LineupRepository lineupRepository,
            LineupImageRepository lineupImageRepository,
            UserRepository userRepository) {
        this.lineupRepository = lineupRepository;
        this.lineupImageRepository = lineupImageRepository;
        this.userRepository = userRepository;
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

    private String generateTitle(Lineup lineup) {
        return lineup.getAgent() + " usando " + lineup.getAbilities() +
                " desde " + lineup.getExecutedOn() +
                " hacia " + lineup.getAffectedArea();
    }

    public List<Lineup> getUserLineupsPaginated(String firebaseUid, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        User user = userRepository.findById(firebaseUid)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return lineupRepository.findByUploadedByOrderByUploadDateDesc(user, pageable).getContent();
    }

    public Optional<Lineup> getLineupById(Long id) {
        return lineupRepository.findById(id);
    }

    public List<Lineup> getGeneralLineupsPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return lineupRepository.findByIsGeneralTrueOrderByUploadDateDesc(pageable).getContent();
    }

    public List<Lineup> getPendingReviewLineups(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return lineupRepository.findByPendingReviewTrueOrderByUploadDateDesc(pageable).getContent();
    }

    public List<Lineup> getAcceptedLineupsByUser(String firebaseUid, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        User user = userRepository.findById(firebaseUid)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return lineupRepository.findByUploadedByAndIsGeneralTrueOrderByUploadDateDesc(user, pageable).getContent();
    }

    public Optional<Lineup> approveLineup(Long id) {
        Optional<Lineup> lineupOpt = lineupRepository.findById(id);
        lineupOpt.ifPresent(lineup -> {
            lineup.setIsGeneral(true);
            lineup.setPendingReview(false);
            lineupRepository.save(lineup);
        });
        return lineupOpt;
    }

    public Optional<Lineup> rejectLineup(Long id) {
        Optional<Lineup> lineupOpt = lineupRepository.findById(id);
        lineupOpt.ifPresent(lineup -> {
            lineup.setIsGeneral(false);
            lineup.setPendingReview(false);
            lineupRepository.save(lineup);
        });
        return lineupOpt;
    }

    public void deleteLineup(Long id) {
        lineupRepository.deleteById(id);
    }

    public List<Lineup> getAllAcceptedLineups(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return lineupRepository
                .findByIsGeneralTrueOrderByUploadDateDesc(pageable)
                .getContent();
    }

    public List<Lineup> findByFilters(String map, String agent, String side, String from, String to) {
        return lineupRepository.findAll().stream()
                .filter(l -> map == null || l.getMap().equalsIgnoreCase(map))
                .filter(l -> agent == null || l.getAgent().equalsIgnoreCase(agent))
                .filter(l -> side == null || l.getSide().equalsIgnoreCase(side))
                .filter(l -> from == null || l.getExecutedOn().toLowerCase().contains(from.toLowerCase()))
                .filter(l -> to == null || l.getAffectedArea().toLowerCase().contains(to.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Lineup> getAllLineupsByUserNoPagination(String firebaseUid) {
        User user = userRepository.findById(firebaseUid)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return lineupRepository
                .findByUploadedByOrderByUploadDateDesc(user, PageRequest.of(0, 9999))
                .getContent();
    }
}
