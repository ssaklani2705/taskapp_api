package com.webelement.taskapp.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.webelement.taskapp.common.CommonFunction;
import lombok.RequiredArgsConstructor;

@Service
public class PasswordService {

	private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
    private final CommonFunction commonFunction;
    
    public PasswordService(@Lazy CommonFunction commonFunction) {
		this.commonFunction = commonFunction;
	}

    public String hash(String raw) {
        return encoder.encode(raw);
    }

    public boolean isHashed(String stored) {
        return stored != null && stored.startsWith("$2");
    }

    public boolean matches(String raw, String stored) {
        if (raw == null || raw.isEmpty() || stored == null || stored.isEmpty()) {
            return false;                                   // blocks empty-password logins
        }
        if (isHashed(stored)) {
            return encoder.matches(raw, stored);
        }
        try {                                               // legacy DES value
            String legacy = commonFunction.decipher(stored);
            return MessageDigest.isEqual(legacy.getBytes(StandardCharsets.UTF_8),
                                         raw.getBytes(StandardCharsets.UTF_8))
                || MessageDigest.isEqual(legacy.getBytes(StandardCharsets.UTF_8),
                                         raw.replace("'", "''").getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }
}