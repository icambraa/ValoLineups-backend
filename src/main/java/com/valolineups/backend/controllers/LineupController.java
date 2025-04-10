package com.valolineups.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.valolineups.backend.models.Lineup;
import com.valolineups.backend.services.LineupService;

import com.valolineups.backend.models.SearchFilterResult;
import com.valolineups.backend.models.SearchRequest;
import com.valolineups.backend.services.OpenAiService;
import com.valolineups.backend.utils.QueryNormalizer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lineups")
@CrossOrigin("*")
public class LineupController {

    private final LineupService lineupService;
    private final OpenAiService openAiService;
    private final QueryNormalizer normalizer;

    public LineupController(LineupService lineupService,
            OpenAiService openAiService,
            QueryNormalizer normalizer) {
        this.lineupService = lineupService;
        this.openAiService = openAiService;
        this.normalizer = normalizer;
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
        lineup.setUploadedBy(uploadedBy);
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
    public ResponseEntity<List<Lineup>> getAllAcceptedLineups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        return ResponseEntity.ok(lineupService.getAllAcceptedLineups(page, size));
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

    private List<Double> parseEmbedding(String jsonArray) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonArray,
                    new com.fasterxml.jackson.core.type.TypeReference<List<Double>>() {
                    });
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    private double cosineSimilarity(List<Double> a, List<Double> b) {
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.size(); i++) {
            dot += a.get(i) * b.get(i);
            normA += Math.pow(a.get(i), 2);
            normB += Math.pow(b.get(i), 2);
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    @PostMapping("/search-intelligent")
    public ResponseEntity<List<Lineup>> searchSmart(@RequestBody SearchRequest request) {
        System.out.println("=== searchSmart() called ===");
        System.out.println(">>> Request query: " + request.getQuery());
        System.out.println(">>> Request map: " + request.getMap()
                + ", agent: " + request.getAgent()
                + ", side: " + request.getSide()
                + ", from: " + request.getFrom()
                + ", to: " + request.getTo()
                + ", uploadedBy: " + request.getUploadedBy());

        // 1) Decidir la base (si uploadedBy => lineups del user, sino => lineups
        // aceptados)
        List<Lineup> baseLineups;
        if (request.getUploadedBy() != null && !request.getUploadedBy().isEmpty()) {
            // TODOS los lineups del user, sin filtrar isGeneral
            baseLineups = lineupService.getAllLineupsByUserNoPagination(request.getUploadedBy());
            System.out.println(">>> baseLineups (user = " + request.getUploadedBy()
                    + ") => " + baseLineups.size() + " lineups");
        } else {
            // lineups aceptados globales
            baseLineups = lineupService.getAllAcceptedLineups(0, 9999);
            System.out.println(">>> baseLineups (ALL accepted) => " + baseLineups.size() + " lineups");
        }

        debugAgentField(baseLineups);

        // 2) Si la query está vacía => filtras exacto con
        // request.map/agent/side/from/to
        String query = request.getQuery() != null ? request.getQuery().trim() : "";
        boolean hasText = !query.isEmpty();

        if (!hasText) {
            List<Lineup> result = baseLineups.stream()
                    .filter(l -> request.getMap() == null
                            || l.getMap().equalsIgnoreCase(request.getMap()))
                    .filter(l -> request.getAgent() == null
                            || l.getAgent().equalsIgnoreCase(request.getAgent()))
                    .filter(l -> request.getSide() == null
                            || l.getSide().equalsIgnoreCase(request.getSide()))
                    .filter(l -> request.getFrom() == null
                            || l.getExecutedOn().toLowerCase().contains(request.getFrom().toLowerCase()))
                    .filter(l -> request.getTo() == null
                            || l.getAffectedArea().toLowerCase().contains(request.getTo().toLowerCase()))
                    .collect(Collectors.toList());

            System.out.println(">>> No text. Found " + result.size() + " lineups after exact filters.");
            return ResponseEntity.ok(result);
        }

        // 3) Si sí hay texto => búsqueda inteligente con embeddings
        String cleanedQuery = normalizer.normalize(query);
        System.out.println(">>> cleanedQuery: " + cleanedQuery);

        SearchFilterResult filters = openAiService.extractFilters(cleanedQuery);
        if (filters == null) {
            System.err.println("!!! extractFilters returned null !!!");
            return ResponseEntity.status(500).build();
        }

        if (filters.map != null && filters.map.trim().isEmpty()) {
            filters.map = null;
        }
        if (filters.agent != null && filters.agent.trim().isEmpty()) {
            filters.agent = null;
        }
        if (filters.side != null && filters.side.trim().isEmpty()) {
            filters.side = null;
        }

        if (filters.executedOn != null && filters.executedOn.trim().isEmpty()) {
            filters.executedOn = null;
        }
        if (filters.affectedArea != null && filters.affectedArea.trim().isEmpty()) {
            filters.affectedArea = null;
        }

        System.out.println(">>> OpenAI filters =>");
        System.out.println("    map: " + filters.map);
        System.out.println("    agent: " + filters.agent);
        System.out.println("    side: " + filters.side);
        System.out.println("    executedOn: " + filters.executedOn);
        System.out.println("    affectedArea: " + filters.affectedArea);

        // 4) Filtramos baseLineups según lo que detectó GPT (map, agent...) y forzamos
        // embedding != null
        List<Lineup> filtered = baseLineups.stream()
                .filter(l -> filters.map == null
                        || l.getMap().equalsIgnoreCase(filters.map))
                .filter(l -> filters.agent == null
                        || l.getAgent().equalsIgnoreCase(filters.agent))
                .filter(l -> filters.side == null
                        || l.getSide().equalsIgnoreCase(filters.side))
                .filter(l -> filters.executedOn == null
                        || l.getExecutedOn().equalsIgnoreCase(filters.executedOn))
                .filter(l -> filters.affectedArea == null
                        || l.getAffectedArea().equalsIgnoreCase(filters.affectedArea))
                .filter(l -> l.getEmbedding() != null)
                .collect(Collectors.toList());

        System.out.println(">>> filtered size (before ranking) = " + filtered.size());

        // 5) Calcular embeddings de la query
        List<Double> queryEmbedding = openAiService.getEmbedding(query);
        if (queryEmbedding == null) {
            System.err.println("!!! getEmbedding returned null !!!");
            return ResponseEntity.status(500).build();
        }

        // 6) Ordenar por similitud
        List<Lineup> sorted = filtered.stream()
                .sorted((a, b) -> {
                    double simA = cosineSimilarity(parseEmbedding(a.getEmbedding()), queryEmbedding);
                    double simB = cosineSimilarity(parseEmbedding(b.getEmbedding()), queryEmbedding);
                    return Double.compare(simB, simA); // mayor->menor
                })
                .limit(10) // top 10
                .collect(Collectors.toList());

        System.out.println(">>> returning " + sorted.size() + " lineups");
        return ResponseEntity.ok(sorted);
    }

    private void debugAgentField(List<Lineup> lineups) {
        System.out.println("---- baseLineups details ----");
        for (Lineup l : lineups) {
            System.out.println("ID=" + l.getId()
                    + " | agent=[" + l.getAgent() + "] len=" + l.getAgent().length());
            for (int i = 0; i < l.getAgent().length(); i++) {
                char c = l.getAgent().charAt(i);
                System.out.println("   agent char[" + i + "] = "
                        + (int) c + " ('" + c + "')");
            }
        }
        System.out.println("---- end baseLineups details ----\n");
    }

}
