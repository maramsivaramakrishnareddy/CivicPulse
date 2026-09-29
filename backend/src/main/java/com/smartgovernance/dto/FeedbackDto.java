package com.smartgovernance.dto;

import java.time.LocalDateTime;

public class FeedbackDto {

    private Long feedbackId;
    private String complaintId;
    private Long citizenId;
    private String citizenName;
    private Integer rating;
    private String comment;
    private String imageUrl;
    private String videoUrl;
    private String location;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;

    public FeedbackDto() {
    }

    public FeedbackDto(Long feedbackId, String complaintId, String citizenName,
                       Integer rating, String comment, LocalDateTime createdAt) {
        this.feedbackId = feedbackId;
        this.complaintId = complaintId;
        this.citizenName = citizenName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public FeedbackDto(Long feedbackId, String complaintId, Long citizenId, String citizenName,
                       Integer rating, String comment, String imageUrl, String videoUrl,
                       String location, Double latitude, Double longitude, LocalDateTime createdAt) {
        this.feedbackId = feedbackId;
        this.complaintId = complaintId;
        this.citizenId = citizenId;
        this.citizenName = citizenName;
        this.rating = rating;
        this.comment = comment;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdAt = createdAt;
    }

    public Long getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(Long feedbackId) {
        this.feedbackId = feedbackId;
    }

    public String getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(String complaintId) {
        this.complaintId = complaintId;
    }

    public Long getCitizenId() {
        return citizenId;
    }

    public void setCitizenId(Long citizenId) {
        this.citizenId = citizenId;
    }

    public String getCitizenName() {
        return citizenName;
    }

    public void setCitizenName(String citizenName) {
        this.citizenName = citizenName;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
