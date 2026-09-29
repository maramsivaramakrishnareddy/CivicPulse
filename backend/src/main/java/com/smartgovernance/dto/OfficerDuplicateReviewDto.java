package com.smartgovernance.dto;

public class OfficerDuplicateReviewDto {
    private OfficerComplaintDetailDto currentComplaint; private OfficerComplaintDetailDto possibleOriginal; private Double similarity; private Double distanceMetres; private String reason;
    public OfficerComplaintDetailDto getCurrentComplaint(){return currentComplaint;} public void setCurrentComplaint(OfficerComplaintDetailDto v){currentComplaint=v;}
    public OfficerComplaintDetailDto getPossibleOriginal(){return possibleOriginal;} public void setPossibleOriginal(OfficerComplaintDetailDto v){possibleOriginal=v;}
    public Double getSimilarity(){return similarity;} public void setSimilarity(Double v){similarity=v;}
    public Double getDistanceMetres(){return distanceMetres;} public void setDistanceMetres(Double v){distanceMetres=v;}
    public String getReason(){return reason;} public void setReason(String v){reason=v;}
}
