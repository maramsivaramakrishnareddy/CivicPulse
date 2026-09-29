package com.smartgovernance.dto;

/**
 * DTO representing current authenticated session state.
 */
public class SessionInfoDto {

    private boolean authenticated;
    private String userType;
    private Long userId;
    private String userName;
    private Object details;

    public SessionInfoDto() {
        this.authenticated = false;
    }

    public SessionInfoDto(boolean authenticated, String userType, Long userId, String userName, Object details) {
        this.authenticated = authenticated;
        this.userType = userType;
        this.userId = userId;
        this.userName = userName;
        this.details = details;
    }

    public static SessionInfoDto unauthenticated() {
        return new SessionInfoDto(false, null, null, null, null);
    }

    public static SessionInfoDto authenticated(String userType, Long userId, String userName, Object details) {
        return new SessionInfoDto(true, userType, userId, userName, details);
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Object getDetails() {
        return details;
    }

    public void setDetails(Object details) {
        this.details = details;
    }
}
