package com.webelement.taskapp.jwttoken;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {
    private CurrentUser() {}
    public static AppUser get() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !(a.getPrincipal() instanceof AppUser)) {
            throw new AccessDeniedException("Not authenticated");
        }
        return (AppUser) a.getPrincipal();
    }
}