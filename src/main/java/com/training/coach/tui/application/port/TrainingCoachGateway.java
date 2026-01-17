package com.training.coach.tui.application.port;

import com.training.coach.athlete.domain.model.Athlete;
import com.training.coach.athlete.domain.model.TrainingPlan;
import com.training.coach.security.AuthService;
import com.training.coach.shared.domain.unit.Hours;
import com.training.coach.user.domain.model.SystemUser;
import java.time.LocalDate;

public interface TrainingCoachGateway {

    AuthService.AuthTokens login(String username, String password);

    Athlete getAthlete(String athleteId);

    TrainingPlan generateTrainingPlan(Athlete athlete, String phase, LocalDate startDate, Hours targetWeeklyHours);

    SystemUser[] listUsers();

    SystemUser createUser(String name, String role, String username, String password);

    void setPassword(String userId, String newPassword);

    String rotatePassword(String userId);

    void disableUser(String userId);

    void enableUser(String userId);
}
