package com.valolineups.backend.dto;

import com.valolineups.backend.models.Lineup;
import com.valolineups.backend.models.LineupImage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record LineupDTO(
        Long id,
        String title,
        String description,
        String map,
        String agent,
        String abilities,
        String affectedArea,
        String executedOn,
        String videoUrl,
        String uploadedBy,
        LocalDateTime uploadDate,
        String side,
        boolean isGeneral,
        List<String> images) {
    public static LineupDTO from(Lineup l) {
        return new LineupDTO(
                l.getId(),
                l.getTitle(),
                l.getDescription(),
                l.getMap(),
                l.getAgent(),
                l.getAbilities(),
                l.getAffectedArea(),
                l.getExecutedOn(),
                l.getVideoUrl(),
                l.getUploadedBy().getDisplayName(),
                l.getUploadDate(),
                l.getSide(),
                l.isGeneral(),
                l.getImages().stream().map(LineupImage::getUrl).collect(Collectors.toList()));
    }
}
