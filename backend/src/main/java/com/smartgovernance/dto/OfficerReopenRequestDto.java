package com.smartgovernance.dto;

import java.time.LocalDateTime;

public class OfficerReopenRequestDto {
    private Long reopenId;
    private Long citizenId;
    private String citizenName;
    private String reason;
    private String status;
    private LocalDateTime createdAt;
    // Evidence submitted by the citizen with the reopen request. This is
    // separate from the complaint's original imageUrl/videoUrl, so it must be
    // surfaced explicitly for the Officer Side to display it.
    private String imageUrl;
    private String videoUrl;
    public Long getReopenId(){return reopenId;} public void setReopenId(Long v){reopenId=v;}
    public Long getCitizenId(){return citizenId;} public void setCitizenId(Long v){citizenId=v;}
    public String getCitizenName(){return citizenName;} public void setCitizenName(String v){citizenName=v;}
    public String getReason(){return reason;} public void setReason(String v){reason=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
    public String getImageUrl(){return imageUrl;} public void setImageUrl(String v){imageUrl=v;}
    public String getVideoUrl(){return videoUrl;} public void setVideoUrl(String v){videoUrl=v;}
}
