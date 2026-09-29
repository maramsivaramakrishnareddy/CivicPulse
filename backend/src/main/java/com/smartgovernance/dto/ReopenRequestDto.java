package com.smartgovernance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ReopenRequestDto {

    @NotBlank(message = "Reopen reason is required")
    private String reason;

    @NotBlank(message = "A live geotagged image is required")
    private String imageUrl;

    @NotBlank(message = "A live video is required")
    private String videoUrl;

    @NotNull(message = "Latitude is required for the evidence")
    private Double latitude;

    @NotNull(message = "Longitude is required for the evidence")
    private Double longitude;

    @NotBlank(message = "A resolved address is required for the evidence")
    private String location;

    public ReopenRequestDto() {
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
