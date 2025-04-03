package com.valolineups.backend.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "lineups")
public class Lineup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String map;
    private String agent;
    private String abilities;
    private String affectedArea;
    private String executedOn;
    private String videoUrl;
    private String uploadedBy;
    private LocalDateTime uploadDate;
    private String side;

    @Column(name = "is_general")
    private boolean isGeneral = false;

    @Column(name = "pending_review")
    private boolean pendingReview = false;

    @OneToMany(mappedBy = "lineup", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<LineupImage> images = new ArrayList<>();

    public Lineup() {
        this.uploadDate = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getAbilities() {
        return abilities;
    }

    public void setAbilities(String abilities) {
        this.abilities = abilities;
    }

    public String getAffectedArea() {
        return affectedArea;
    }

    public void setAffectedArea(String affectedArea) {
        this.affectedArea = affectedArea;
    }

    public String getExecutedOn() {
        return executedOn;
    }

    public void setExecutedOn(String executedOn) {
        this.executedOn = executedOn;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public boolean isGeneral() {
        return isGeneral;
    }

    public void setIsGeneral(boolean isGeneral) {
        this.isGeneral = isGeneral;
    }

    public boolean isPendingReview() {
        return pendingReview;
    }

    public void setPendingReview(boolean pendingReview) {
        this.pendingReview = pendingReview;
    }

    public List<LineupImage> getImages() {
        return images;
    }

    public void setImages(List<LineupImage> images) {
        this.images = images;
    }
}
