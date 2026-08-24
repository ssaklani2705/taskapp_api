package com.webelement.taskapp.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {
	private Set<String> blacklist = ConcurrentHashMap.newKeySet();

	public void blacklistToken(String jti) {
		blacklist.add(jti);
	}

	public boolean isBlacklisted(String jti) {
		return blacklist.contains(jti);
	}
}
