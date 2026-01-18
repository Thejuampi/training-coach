package com.training.coach.tui.application.port;

import com.training.coach.common.AuthTokens;
import com.training.coach.shared.domain.unit.Hours;
import com.training.coach.tui.dto.Athlete;
import com.training.coach.tui.dto.SystemUser;
import com.training.coach.tui.dto.TrainingPlan;
import java.time.LocalDate;

public interface TrainingCoachGateway {

    AuthTokens login(String username, String password);

    Athlete getAthlete(String athleteId);

    TrainingPlan generateTrainingPlan(Athlete athlete, String phase, LocalDate startDate, Hours targetWeeklyHours);

    SystemUser[] listUsers();

    SystemUser createUser(String name, String role, String username, String password);

    void setPassword(String userId, String newPassword);

    String rotatePassword(String userId);

    void disableUser(String userId);

    void enableUser(String userId);
}