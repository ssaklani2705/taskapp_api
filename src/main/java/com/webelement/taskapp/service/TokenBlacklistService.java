package com.webelement.taskapp.service;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {
    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    public void blacklistToken(String jti, Date expiry) {
        blacklist.put(jti, expiry.getTime());
    }
    public boolean isBlacklisted(String jti) {
        return blacklist.containsKey(jti);
    }
    @Scheduled(fixedRate = 3_600_000)           // hourly cleanup of expired entries
    public void purge() {
        long now = System.currentTimeMillis();
        blacklist.values().removeIf(exp -> exp < now);
    }
}