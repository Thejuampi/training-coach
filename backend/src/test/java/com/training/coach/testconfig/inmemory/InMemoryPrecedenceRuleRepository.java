package com.training.coach.testconfig.inmemory;

import com.training.coach.reconciliation.application.port.out.PrecedenceRuleRepository;
import com.training.coach.reconciliation.domain.model.PrecedenceRule;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory PrecedenceRuleRepository for fast tests.
 */
public class InMemoryPrecedenceRuleRepository implements PrecedenceRuleRepository {
    private final ConcurrentHashMap<String, PrecedenceRule> rules = new ConcurrentHashMap<>();

    @Override
    public PrecedenceRule save(PrecedenceRule rule) {
        rules.put(rule.id(), rule);
        return rule;
    }

    @Override
    public Optional<PrecedenceRule> findById(String ruleId) {
        return Optional.ofNullable(rules.get(ruleId));
    }

    @Override
    public Optional<PrecedenceRule> findActiveByAthleteId(String athleteId) {
        return rules.values().stream()
                .filter(r -> r.athleteId().equals(athleteId) && r.isActive())
                .findFirst();
    }

    @Override
    public List<PrecedenceRule> findByAthleteId(String athleteId) {
        return rules.values().stream()
                .filter(r -> r.athleteId().equals(athleteId))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String ruleId) {
        rules.remove(ruleId);
    }

    /**
     * Clear all rules for testing purposes.
     */
    public void clearAll() {
        rules.clear();
    }
}
