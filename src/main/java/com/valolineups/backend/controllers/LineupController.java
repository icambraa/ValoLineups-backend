package com.valolineups.backend.controllers;

import com.valolineups.backend.models.Lineup;
import com.valolineups.backend.services.LineupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lineups")
@CrossOrigin("*")
public class LineupController {

    private final LineupService lineupService;

    // Ya NO necesitas el CloudinaryService aquí,
    // porque subes a Cloudinary desde el FRONT
    public LineupController(LineupService lineupService) {
        this.lineupService = lineupService;
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
            @RequestParam("uploadedBy") String uploadedBy,
            @RequestParam(value = "images", required = false) List<String> imageUrls) {

        Lineup lineup = new Lineup();
        lineup.setDescription(description);
        lineup.setMap(map);
        lineup.setAgent(agent);
        lineup.setAbilities(abilities);
        lineup.setAffectedArea(affectedArea);
        lineup.setExecutedOn(executedOn);
        lineup.setVideoUrl(videoUrl);
        lineup.setSide(side);
        lineup.setUploadedBy(uploadedBy);

        if (imageUrls != null && !imageUrls.isEmpty()) {
            lineup.setImageUrl(String.join(",", imageUrls));
        } else {
            lineup.setImageUrl("");
        }

        Lineup savedLineup = lineupService.createLineup(lineup);
        return ResponseEntity.ok(savedLineup);
    }

    @GetMapping
    public ResponseEntity<List<Lineup>> getAllLineups() {
        return ResponseEntity.ok(lineupService.getAllLineups());
    }
}
