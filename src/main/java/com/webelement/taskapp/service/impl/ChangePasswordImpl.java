package com.webelement.taskapp.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.webelement.taskapp.Exceptions.PasswordValidationException;
import com.webelement.taskapp.common.CommonFunction;
import com.webelement.taskapp.dto.AssigneeWorkloadDTO;
import com.webelement.taskapp.dto.ChangePassWordDTO;
import com.webelement.taskapp.jwttoken.JwtUtil;
import com.webelement.taskapp.repo.UserLoginRepository;
import com.webelement.taskapp.service.ChangePassword;
import com.webelement.taskapp.service.JwtService;
import com.webelement.taskapp.service.TokenBlacklistService;
import com.webelement.taskapp.service.UserAccessLogService;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class ChangePasswordImpl implements ChangePassword {

	private final UserLoginRepository userReposatroy;
	private final CommonFunction commonFunction;


	@Override
	@Transactional
	public String changePassword(ChangePassWordDTO request) {

	   
	    validateChangePasswordRequest(request);

	    String emailId;
	    String oldPassword;
	    String newPassword;
	    String decryptedUserId;

	   
	    try {
	        emailId = commonFunction.decipher(request.getUsername()).trim();
	        oldPassword = commonFunction.decipher(request.getOldPassword());
	        newPassword = commonFunction.decipher(request.getNewPassword());
	        decryptedUserId = commonFunction.decipher(request.getUserId());

	    } catch (Exception e) {
	        throw new PasswordValidationException(
	                "Invalid encrypted request data.", e);
	    }

	    validateDecryptedValues(emailId, oldPassword, newPassword);
	    validateNewPassword(oldPassword, newPassword);

	   
	    Integer requestedUserId;

	    try {
	        requestedUserId = Integer.valueOf(decryptedUserId.trim());
	    } catch (Exception e) {
	        throw new PasswordValidationException("Invalid user ID.");
	    }

	    Integer actualUserId = userReposatroy
	            .findUserIdByEmailAndStatusAndUserId(
	                    emailId, requestedUserId)
	            .orElseThrow(() -> new PasswordValidationException(
	                    "Email ID and user ID do not match, or the user is inactive."));

	
	    String storedPassword =
	            userReposatroy.findPasswordByUserId(requestedUserId);
	    
	    
	    
	    if (isBlank(storedPassword)) {
	        throw new IllegalStateException(
	                "Unable to retrieve the existing password.");
	    }


	    String existingPassword;

	    try {
	        existingPassword = commonFunction.decipher(storedPassword);
	    } catch (Exception e) {
	        throw new IllegalStateException(
	                "Unable to verify the existing password.", e);
	    }

	    
	    if (existingPassword == null
	            || !existingPassword.equals(oldPassword)) {
	        throw new PasswordValidationException(
	                "Old password is incorrect.");
	    }

	   
	    String encryptedPassword;

	    try {
	        encryptedPassword = commonFunction.cipher(newPassword);
	    } catch (Exception e) {
	        throw new IllegalStateException(
	                "Unable to encrypt the new password.", e);
	    }

	    if (isBlank(encryptedPassword)) {
	        throw new IllegalStateException(
	                "Unable to encrypt the new password.");
	    }

	  
	    int updatedRows = userReposatroy.updateForgotPassword(
	            actualUserId,
	            encryptedPassword,
	            null
	    );

	    if (updatedRows != 1) {
	        throw new IllegalStateException(
	                "Password update failed. Please try again.");
	    }

	    return "Password changed successfully.";
	}
	

	private void validateChangePasswordRequest(ChangePassWordDTO request) {

		if (request == null) {
			throw new PasswordValidationException("Request cannot be null.");
		}

		if (isBlank(request.getUsername())) {
			throw new PasswordValidationException("Email ID is required.");
		}

		if (isBlank(request.getOldPassword())) {
			throw new PasswordValidationException("Old password is required.");
		}

		if (isBlank(request.getNewPassword())) {
			throw new PasswordValidationException("New password is required.");
		}
	}
	
	private void validateDecryptedValues(String emailId, String oldPassword, String newPassword) {

		if (isBlank(emailId) || isBlank(oldPassword) || isBlank(newPassword)) {
			throw new PasswordValidationException("Invalid email or password details.");
		}
	}

	private void validateNewPassword(String oldPassword, String newPassword) {
		if (newPassword.length() < 8) {
			throw new PasswordValidationException("New password must contain at least 8 characters.");
		}
		if (oldPassword.equals(newPassword)) {
			throw new PasswordValidationException("New password must be different from the old password.");
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

}
