package com.webelement.taskapp.controller;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.RefreshTokenResponse;
import com.webelement.taskapp.jwttoken.AppUser;
import com.webelement.taskapp.jwttoken.CurrentUser;
import com.webelement.taskapp.jwttoken.JwtUtil;
import com.webelement.taskapp.service.JwtService;
import com.webelement.taskapp.service.MyUserDetailsService;
import com.webelement.taskapp.service.TokenBlacklistService;
import com.webelement.taskapp.service.UserAccessLogService;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@RequiredArgsConstructor
public class LogoutController {

	private final JwtUtil jwtUtil;
	private final TokenBlacklistService tokenBlacklistService;
	private final JwtService jwtService;
	private final UserAccessLogService accessLogService;
	private final MyUserDetailsService detailsService;

	@PostMapping("/refreshtoken")
	public ApiResponse<RefreshTokenResponse> refreshToken(HttpServletRequest request) {
	    try {
	        String oldToken = jwtService.extractTokenFromRequest(request);
	        if (oldToken == null) {
	            return new ApiResponse<>(false, "Authorization header missing", null);
	        }

	        // Throws if the signature is bad or the token is expired
	        Claims claims = jwtUtil.extractAllClaims(oldToken);
	        String username = claims.getSubject();

	        // Covers expiry and the logout blacklist
	        if (!jwtUtil.validateToken(oldToken, username)) {
	            return new ApiResponse<>(false, "Invalid or expired token", null);
	        }

	        String loginType = claims.get("lt", String.class);
	        if (loginType == null) {
	            return new ApiResponse<>(false, "Please login again", null);   // pre-upgrade token
	        }

	        // Fails if the user was deactivated, so refresh can't keep them alive
	        AppUser user = detailsService.loadAppUser(username, loginType);

	        String newToken = jwtUtil.generateAccessToken(username, user.getUserId(), loginType);
	        return new ApiResponse<>(true, "Token refreshed successfully", new RefreshTokenResponse(newToken));

	    } catch (Exception e) {
	        return new ApiResponse<>(false, "Token refresh failed", null);
	    }
	}

	@PostMapping("/logout")
	public ResponseEntity<?> logout(@RequestHeader("Authorization") String token,
	        @RequestBody Map<String, String> body) {
	    Integer sessionId;
	    try {
	        sessionId = Integer.parseInt(body.get("sessionId"));
	    } catch (NumberFormatException e) {
	        return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Invalid sessionId"));
	    }
	    try {
	        int userId = CurrentUser.get().getUserId();
	        accessLogService.updateLogout(sessionId, userId);     // only this user's own session

	        Claims claims = jwtUtil.extractAllClaims(token.substring(7));
	        tokenBlacklistService.blacklistToken(claims.getId(), claims.getExpiration());

	        return ResponseEntity.ok(Collections.singletonMap("message", "Logged out successfully"));
	    } catch (Exception ex) {
	        return ResponseEntity.status(401).body(Collections.singletonMap("error", "Invalid token"));
	    }
	}

}