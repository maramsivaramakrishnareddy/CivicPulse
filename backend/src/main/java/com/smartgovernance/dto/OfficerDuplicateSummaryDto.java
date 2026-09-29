package com.smartgovernance.dto;

import java.time.LocalDateTime;

public class OfficerDuplicateSummaryDto {
    private String complaintId,title,departmentName,location,status,duplicateStatus,possibleOriginalId; private String complaintType; private Double similarity,distanceMetres; private LocalDateTime createdAt;
    public String getComplaintId(){return complaintId;} public void setComplaintId(String v){complaintId=v;}
    public String getTitle(){return title;} public void setTitle(String v){title=v;}
    public String getDepartmentName(){return departmentName;} public void setDepartmentName(String v){departmentName=v;}
    public String getLocation(){return location;} public void setLocation(String v){location=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public String getDuplicateStatus(){return duplicateStatus;} public void setDuplicateStatus(String v){duplicateStatus=v;}
    public String getPossibleOriginalId(){return possibleOriginalId;} public void setPossibleOriginalId(String v){possibleOriginalId=v;}
    public String getComplaintType(){return complaintType;} public void setComplaintType(String v){complaintType=v;}
    public Double getSimilarity(){return similarity;} public void setSimilarity(Double v){similarity=v;}
    public Double getDistanceMetres(){return distanceMetres;} public void setDistanceMetres(Double v){distanceMetres=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
