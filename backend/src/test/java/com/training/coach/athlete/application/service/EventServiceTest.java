package com.training.coach.athlete.application.service;

import com.training.coach.athlete.application.port.out.EventRepository;
import com.training.coach.athlete.domain.model.Event;
import com.training.coach.athlete.domain.model.TaperPeriod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private com.training.coach.trainingplan.application.service.PlanService planService;

    @Mock
    private com.training.coach.trainingplan.application.service.PlanRebaseService planRebaseService;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventService(eventRepository, planService, planRebaseService);
    }

    @Test
    void addEvent_shouldCreateAndSaveEvent() {
        // Given
        String athleteId = "athlete-123";
        String eventName = "Spring Classic";
        LocalDate eventDate = LocalDate.of(2026, 3, 1);
        Event.EventPriority priority = Event.EventPriority.A;

        Event mockEvent = Event.create(athleteId, eventName, eventDate, priority);
        when(eventRepository.save(any(Event.class))).thenReturn(mockEvent);

        // When
        var result = eventService.addEvent(athleteId, eventName, eventDate, priority);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.value()).isPresent();
        assertThat(result.value().get().name()).isEqualTo(eventName);
        assertThat(result.value().get().date()).isEqualTo(eventDate);
        assertThat(result.value().get().priority()).isEqualTo(priority);

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void getEvents_shouldReturnAllEventsForAthlete() {
        // Given
        String athleteId = "athlete-123";
        List<Event> events = List.of(
            Event.create(athleteId, "Event 1", LocalDate.now(), Event.EventPriority.A),
            Event.create(athleteId, "Event 2", LocalDate.now().plusWeeks(1), Event.EventPriority.B)
        );
        when(eventRepository.findByAthleteId(athleteId)).thenReturn(events);

        // When
        List<Event> result = eventService.getEvents(athleteId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyElementsOf(events);
    }

    @Test
    void getEvent_shouldReturnEventWhenExists() {
        // Given
        String eventId = "event-123";
        Event event = Event.create("athlete-123", "Test Event", LocalDate.now(), Event.EventPriority.A);
        when(eventRepository.findById(eventId)).thenReturn(event);

        // When
        Optional<Event> result = eventService.getEvent(eventId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(event.id());
    }

    @Test
    void updateEventDate_shouldUpdateDateAndTriggerRebase() {
        // Given
        String eventId = "event-123";
        LocalDate oldDate = LocalDate.of(2026, 3, 1);
        LocalDate newDate = LocalDate.of(2026, 3, 15);
        Event event = Event.create("athlete-123", "Spring Classic", oldDate, Event.EventPriority.A);

        when(eventRepository.findById(eventId)).thenReturn(event);
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(planService.getPlansForAthlete(any())).thenReturn(List.of());

        // When
        var result = eventService.updateEventDate(eventId, newDate);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.value()).isPresent();
        assertThat(result.value().get().date()).isEqualTo(newDate);

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void calculateTaper_shouldReturnCorrectTaperPeriod() {
        // Given
        Event event = Event.create("athlete-123", "A Race", LocalDate.of(2026, 3, 1), Event.EventPriority.A);

        // When
        TaperPeriod taper = eventService.calculateTaper(event);

        // Then
        assertThat(taper.eventId()).isEqualTo(event.id());
        assertThat(taper.endDate()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(taper.durationDays()).isEqualTo(14); // 2 weeks for A-priority
        assertThat(taper.phase()).isEqualTo(TaperPeriod.TaperPhase.FULL_TAPER);
        assertThat(taper.maintainsKeyEfforts()).isTrue();
    }

    @Test
    void calculateTaper_forBPriorityEvent_shouldReturnShorterTaper() {
        // Given
        Event event = Event.create("athlete-123", "B Race", LocalDate.of(2026, 3, 1), Event.EventPriority.B);

        // When
        TaperPeriod taper = eventService.calculateTaper(event);

        // Then
        assertThat(taper.durationDays()).isEqualTo(7); // 1 week for B-priority
        assertThat(taper.phase()).isEqualTo(TaperPeriod.TaperPhase.MODERATE_TAPER);
    }

    @Test
    void getAPriorityEvents_shouldReturnOnlyAPriorityEvents() {
        // Given
        String athleteId = "athlete-123";
        List<Event> events = List.of(
            Event.create(athleteId, "A Race", LocalDate.now(), Event.EventPriority.A),
            Event.create(athleteId, "B Race", LocalDate.now().plusWeeks(1), Event.EventPriority.B),
            Event.create(athleteId, "C Race", LocalDate.now().plusWeeks(2), Event.EventPriority.C)
        );
        when(eventRepository.findByAthleteId(athleteId)).thenReturn(events);

        // When
        List<Event> result = eventService.getAPriorityEvents(athleteId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).priority()).isEqualTo(Event.EventPriority.A);
    }

    @Test
    void getUpcomingEvents_shouldReturnEventsAfterDate() {
        // Given
        String athleteId = "athlete-123";
        LocalDate today = LocalDate.now();
        List<Event> events = List.of(
            Event.create(athleteId, "Past Event", today.minusDays(1), Event.EventPriority.A),
            Event.create(athleteId, "Future Event 1", today.plusDays(7), Event.EventPriority.B),
            Event.create(athleteId, "Future Event 2", today.plusDays(14), Event.EventPriority.A)
        );
        when(eventRepository.findByAthleteId(athleteId)).thenReturn(events);

        // When
        List<Event> result = eventService.getUpcomingEvents(athleteId, today);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(event -> event.date().isAfter(today));
    }
}