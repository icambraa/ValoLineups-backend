package com.valolineups.backend.controllers;

import com.valolineups.backend.dto.LineupDTO;
import com.valolineups.backend.models.Lineup;
import com.valolineups.backend.repositories.UserRepository;
import com.valolineups.backend.services.LineupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.valolineups.backend.models.User;


import java.util.List;

@RestController
@RequestMapping("/api/lineups")
@CrossOrigin("*")
public class LineupController {

    private final LineupService lineupService;
    private final UserRepository userRepository;

    public LineupController(LineupService lineupService, UserRepository userRepository) {
        this.lineupService = lineupService;
        this.userRepository = userRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<Lineup> createLineup(
            @RequestParam("description") String description,
            @RequestParam("map") String map,
            @RequestParam("agent") String agent,
            @RequestParam("abilities") String abilities,
            @RequestParam("affectedArea") String affectedArea,
            @RequestParam("executedOn") String executedOn,
            @RequestParam("videoUrl") String videoUrl,
            @RequestParam("side") String side,
            @RequestParam("uploadedBy") String firebaseUid,
            @RequestParam(value = "images", required = false) List<String> imageUrls,
            @RequestParam(value = "wantsToBeReviewed", defaultValue = "false") boolean wantsToBeReviewed) {

        Lineup lineup = new Lineup();
        lineup.setDescription(description);
        lineup.setMap(map);
        lineup.setAgent(agent);
        lineup.setAbilities(abilities);
        lineup.setAffectedArea(affectedArea);
        lineup.setExecutedOn(executedOn);
        lineup.setVideoUrl(videoUrl);
        lineup.setSide(side);
        User user = userRepository.findById(firebaseUid)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        lineup.setUploadedBy(user);
        lineup.setPendingReview(wantsToBeReviewed);
        lineup.setIsGeneral(false);

        return ResponseEntity.ok(lineupService.createLineupWithImages(lineup, imageUrls));
    }

    @GetMapping("/user")
    public ResponseEntity<List<Lineup>> getUserLineups(
            @RequestParam String uploadedBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        return ResponseEntity.ok(lineupService.getUserLineupsPaginated(uploadedBy, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lineup> getLineupById(@PathVariable Long id) {
        return lineupService.getLineupById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/accepted")
    public ResponseEntity<List<Lineup>> getAcceptedLineupsByUser(
            @RequestParam String uploadedBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        return ResponseEntity.ok(lineupService.getAcceptedLineupsByUser(uploadedBy, page, size));
    }

    @GetMapping("/accepted")
    public ResponseEntity<List<LineupDTO>> getAllAcceptedLineups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {

        List<Lineup> lineups = lineupService.getAllAcceptedLineups(page, size);
        List<LineupDTO> dtos = lineups.stream().map(LineupDTO::from).toList();
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Lineup> approveLineup(@PathVariable Long id) {
        return lineupService.approveLineup(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Lineup> rejectLineup(@PathVariable Long id) {
        return lineupService.rejectLineup(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Lineup>> getPendingLineups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(lineupService.getPendingReviewLineups(page, size));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLineup(@PathVariable Long id) {
        lineupService.deleteLineup(id);
        return ResponseEntity.noContent().build();
    }
}
