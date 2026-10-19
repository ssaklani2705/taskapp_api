package com.webelement.taskapp.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.webelement.taskapp.dto.LoginRequest;
import com.webelement.taskapp.entity.UserLoginEntity;
import com.webelement.taskapp.jwttoken.AppUser;
import com.webelement.taskapp.repo.UserLoginRepository;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserLoginRepository loginRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return loadAppUser(username, "other");
    }

    public AppUser loadAppUser(String username, String loginType) {
        UserLoginEntity u = loginRepository.findByEmailAndStatus(username, 1)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        boolean admin = "Y".equalsIgnoreCase(u.getPermission());
        boolean hod = "Y".equalsIgnoreCase(u.getIsHod());

        List<GrantedAuthority> auth = new ArrayList<>();
        auth.add(new SimpleGrantedAuthority("manager".equals(loginType) ? "ROLE_MANAGER" : "ROLE_EMPLOYEE"));
        if (admin) auth.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (hod) auth.add(new SimpleGrantedAuthority("ROLE_HOD"));

        return new AppUser(u.getEmail(), auth, u.getUserId(), loginType, admin, hod);
    }
}