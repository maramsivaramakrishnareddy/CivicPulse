package com.smartgovernance.dto;

import java.time.LocalDateTime;

public class OfficerEscalationSummaryDto {
    private String complaintId,title,reason,complaintType,status,escalationStatus,target,officerName; private Integer escalationLevel; private LocalDateTime createdAt;
    public String getComplaintId(){return complaintId;} public void setComplaintId(String v){complaintId=v;}
    public String getTitle(){return title;} public void setTitle(String v){title=v;}
    public String getReason(){return reason;} public void setReason(String v){reason=v;}
    public String getComplaintType(){return complaintType;} public void setComplaintType(String v){complaintType=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public String getEscalationStatus(){return escalationStatus;} public void setEscalationStatus(String v){escalationStatus=v;}
    public String getTarget(){return target;} public void setTarget(String v){target=v;}
    public String getOfficerName(){return officerName;} public void setOfficerName(String v){officerName=v;}
    public Integer getEscalationLevel(){return escalationLevel;} public void setEscalationLevel(Integer v){escalationLevel=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
