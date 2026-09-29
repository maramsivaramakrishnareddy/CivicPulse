package com.smartgovernance.dto;

import jakarta.validation.constraints.NotBlank;

public class EscalationRequest {

    @NotBlank(message = "Escalation reason is required")
    private String reason;

    private Integer escalationLevel = 1;

    private String remarks;

    private String target;

    public EscalationRequest() {
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Integer getEscalationLevel() {
        return escalationLevel;
    }

    public void setEscalationLevel(Integer escalationLevel) {
        this.escalationLevel = escalationLevel;
    }
}
