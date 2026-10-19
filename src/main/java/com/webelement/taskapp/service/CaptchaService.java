package com.webelement.taskapp.service;

import com.webelement.taskapp.dto.CaptchaDto;

public interface CaptchaService {
	public CaptchaDto generateCaptcha();

	public boolean validateCaptcha(CaptchaDto request);
}
