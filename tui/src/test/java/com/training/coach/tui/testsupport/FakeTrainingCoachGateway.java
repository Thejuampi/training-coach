package com.training.coach.tui.testsupport;

import com.training.coach.common.AuthTokens;
import com.training.coach.shared.domain.unit.Hours;
import com.training.coach.tui.application.port.TrainingCoachGateway;
import com.training.coach.tui.dto.Athlete;
import com.training.coach.tui.dto.SystemUser;
import com.training.coach.tui.dto.TrainingPlan;
import java.time.LocalDate;
public class FakeTrainingCoachGateway implements TrainingCoachGateway {

    private AuthTokens loginTokens;
    private RuntimeException loginException;
    private Athlete athlete;
    private TrainingPlan generatedPlan;
    private SystemUser[] users = new SystemUser[0];
    private SystemUser createdUser;

    public String lastLoginUsername;
    public String lastLoginPassword;
    public Athlete lastGeneratedAthlete;
    public String lastGeneratedPhase;
    public LocalDate lastGeneratedStartDate;
    public Hours lastGeneratedWeeklyHours;
    public String lastSetPasswordUserId;
    public String lastSetPassword;
    public String lastRotatePasswordUserId;
    public String lastDisableUserId;
    public String lastEnableUserId;

    public void withLoginTokens(AuthTokens tokens) {
        this.loginTokens = tokens;
    }

    public void withLoginException(RuntimeException exception) {
        this.loginException = exception;
    }

    public void withAthlete(Athlete athlete) {
        this.athlete = athlete;
    }

    public void withGeneratedPlan(TrainingPlan plan) {
        this.generatedPlan = plan;
    }

    public void withUsers(SystemUser... users) {
        this.users = users;
    }

    public void withCreatedUser(SystemUser user) {
        this.createdUser = user;
    }

    @Override
    public AuthTokens login(String username, String password) {
        this.lastLoginUsername = username;
        this.lastLoginPassword = password;
        if (loginException != null) {
            throw loginException;
        }
        return loginTokens;
    }

    @Override
    public Athlete getAthlete(String athleteId) {
        return athlete;
    }

    @Override
    public TrainingPlan generateTrainingPlan(
            Athlete athlete, String phase, LocalDate startDate, Hours targetWeeklyHours) {
        this.lastGeneratedAthlete = athlete;
        this.lastGeneratedPhase = phase;
        this.lastGeneratedStartDate = startDate;
        this.lastGeneratedWeeklyHours = targetWeeklyHours;
        return generatedPlan;
    }

    @Override
    public SystemUser[] listUsers() {
        return users;
    }

    @Override
    public SystemUser createUser(String name, String role, String username, String password) {
        return createdUser;
    }

    @Override
    public void setPassword(String userId, String newPassword) {
        this.lastSetPasswordUserId = userId;
        this.lastSetPassword = newPassword;
    }

    @Override
    public String rotatePassword(String userId) {
        this.lastRotatePasswordUserId = userId;
        return "rotated-password";
    }

    @Override
    public void disableUser(String userId) {
        this.lastDisableUserId = userId;
    }

    @Override
    public void enableUser(String userId) {
        this.lastEnableUserId = userId;
    }
}
