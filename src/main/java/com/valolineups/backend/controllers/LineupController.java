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

    public LineupController(LineupService lineupService) {
        this.lineupService = lineupService;
    }

    @PostMapping("/create")
    public ResponseEntity<Lineup> createLineup(@RequestBody Lineup lineup) {
        Lineup savedLineup = lineupService.createLineup(lineup);
        return ResponseEntity.ok(savedLineup);
    }

    @GetMapping
    public ResponseEntity<List<Lineup>> getAllLineups() {
        return ResponseEntity.ok(lineupService.getAllLineups());
    }
}
