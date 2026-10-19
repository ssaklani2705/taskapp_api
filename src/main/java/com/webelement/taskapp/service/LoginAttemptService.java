package com.webelement.taskapp.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {
    private static final int MAX_FAILS = 5;
    private static final long LOCK_MS = 5 * 60 * 1000L;

    private static class Info { int fails; long lockedUntil; }
    private final Map<String, Info> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {
        Info i = attempts.get(key);
        return i != null && i.lockedUntil > System.currentTimeMillis();
    }

    public void failed(String key) {
        long now = System.currentTimeMillis();
        attempts.compute(key, (k, i) -> {
            if (i == null || (i.lockedUntil != 0 && i.lockedUntil < now)) i = new Info();
            i.fails++;
            if (i.fails >= MAX_FAILS) i.lockedUntil = now + LOCK_MS;
            return i;
        });
    }

    public void succeeded(String key) { attempts.remove(key); }

    @Scheduled(fixedRate = 3_600_000)
    public void purge() {
        long now = System.currentTimeMillis();
        attempts.values().removeIf(i -> i.lockedUntil != 0 && i.lockedUntil < now);
    }
}