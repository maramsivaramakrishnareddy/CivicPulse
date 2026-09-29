package com.smartgovernance.dto;

import java.time.LocalDateTime;

public class OfficerEscalationDetailDto {
    private Long escalationId; private String reason; private Integer escalationLevel; private String status; private String remarks; private String target; private String officerName; private LocalDateTime createdAt;
    public Long getEscalationId(){return escalationId;} public void setEscalationId(Long v){escalationId=v;}
    public String getReason(){return reason;} public void setReason(String v){reason=v;}
    public Integer getEscalationLevel(){return escalationLevel;} public void setEscalationLevel(Integer v){escalationLevel=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public String getRemarks(){return remarks;} public void setRemarks(String v){remarks=v;}
    public String getTarget(){return target;} public void setTarget(String v){target=v;}
    public String getOfficerName(){return officerName;} public void setOfficerName(String v){officerName=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
