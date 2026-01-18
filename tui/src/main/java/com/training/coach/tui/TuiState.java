package com.training.coach.tui;

import com.training.coach.tui.dto.TrainingPlan;
import com.training.coach.tui.dto.UserRole;

public class TuiState {

    private String baseUrl = "http://localhost:8080";
    private String username;
    private String password;
    private String accessToken;
    private String refreshToken;
    private String sessionMessage;
    private String userId;
    private UserRole userRole = UserRole.COACH;
    private String athleteId;
    private String planPhase;
    private String planStartDate;
    private String planTargetWeeklyHours;
    private TrainingPlan lastGeneratedPlan;
    private String planError;
    private String adminNewUserName;
    private String adminNewUserRole;
    private String adminNewUserUsername;
    private String adminNewUserPassword;
    private String adminTargetUserId;
    private String adminNewPassword;
    private String adminConfirmAction;
    private String adminMessage;

    public String baseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String userId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String username() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String password() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String accessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String refreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String sessionMessage() {
        return sessionMessage;
    }

    public void setSessionMessage(String sessionMessage) {
        this.sessionMessage = sessionMessage;
    }

    public UserRole userRole() {
        return userRole;
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }

    public String athleteId() {
        return athleteId;
    }

    public void setAthleteId(String athleteId) {
        this.athleteId = athleteId;
    }

    public String planPhase() {
        return planPhase;
    }

    public void setPlanPhase(String planPhase) {
        this.planPhase = planPhase;
    }

    public String planStartDate() {
        return planStartDate;
    }

    public void setPlanStartDate(String planStartDate) {
        this.planStartDate = planStartDate;
    }

    public String planTargetWeeklyHours() {
        return planTargetWeeklyHours;
    }

    public void setPlanTargetWeeklyHours(String planTargetWeeklyHours) {
        this.planTargetWeeklyHours = planTargetWeeklyHours;
    }

    public TrainingPlan lastGeneratedPlan() {
        return lastGeneratedPlan;
    }

    public void setLastGeneratedPlan(TrainingPlan lastGeneratedPlan) {
        this.lastGeneratedPlan = lastGeneratedPlan;
    }

    public String planError() {
        return planError;
    }

    public void setPlanError(String planError) {
        this.planError = planError;
    }

    public String adminNewUserName() {
        return adminNewUserName;
    }

    public void setAdminNewUserName(String adminNewUserName) {
        this.adminNewUserName = adminNewUserName;
    }

    public String adminNewUserRole() {
        return adminNewUserRole;
    }

    public void setAdminNewUserRole(String adminNewUserRole) {
        this.adminNewUserRole = adminNewUserRole;
    }

    public String adminNewUserUsername() {
        return adminNewUserUsername;
    }

    public void setAdminNewUserUsername(String adminNewUserUsername) {
        this.adminNewUserUsername = adminNewUserUsername;
    }

    public String adminNewUserPassword() {
        return adminNewUserPassword;
    }

    public void setAdminNewUserPassword(String adminNewUserPassword) {
        this.adminNewUserPassword = adminNewUserPassword;
    }

    public String adminTargetUserId() {
        return adminTargetUserId;
    }

    public void setAdminTargetUserId(String adminTargetUserId) {
        this.adminTargetUserId = adminTargetUserId;
    }

    public String adminNewPassword() {
        return adminNewPassword;
    }

    public void setAdminNewPassword(String adminNewPassword) {
        this.adminNewPassword = adminNewPassword;
    }

    public String adminConfirmAction() {
        return adminConfirmAction;
    }

    public void setAdminConfirmAction(String adminConfirmAction) {
        this.adminConfirmAction = adminConfirmAction;
    }

    public String adminMessage() {
        return adminMessage;
    }

    public void setAdminMessage(String adminMessage) {
        this.adminMessage = adminMessage;
    }
}
