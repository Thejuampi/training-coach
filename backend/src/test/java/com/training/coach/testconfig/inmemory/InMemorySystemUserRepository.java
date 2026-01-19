package com.training.coach.testconfig.inmemory;

import com.training.coach.user.application.port.out.SystemUserRepository;
import com.training.coach.user.domain.model.SystemUser;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory SystemUserRepository for fast tests.
 */
public class InMemorySystemUserRepository implements SystemUserRepository {
    private final ConcurrentHashMap<String, SystemUser> users = new ConcurrentHashMap<>();

    @Override
    public SystemUser save(SystemUser user) {
        users.put(user.id(), user);
        return user;
    }

    @Override
    public Optional<SystemUser> findById(String id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<SystemUser> findAll() {
        return List.copyOf(users.values());
    }

    @Override
    public void deleteById(String id) {
        users.remove(id);
    }
}
