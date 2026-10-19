package com.webelement.taskapp.service.impl;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.webelement.taskapp.Exceptions.CaptchaValidationException;
import com.webelement.taskapp.dto.CaptchaDto;
import com.webelement.taskapp.entity.captchaEntity;
import com.webelement.taskapp.repo.CaptchaRepository;
import com.webelement.taskapp.service.CaptchaService;
import com.webelement.taskapp.service.ClientService;
import com.webelement.taskapp.service.RecurringService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CaptchaServiceImpl implements CaptchaService {
	private final CaptchaRepository captchaRepository;
	private final SecureRandom secureRandom = new SecureRandom();

	@Override
	public CaptchaDto generateCaptcha() {
		int captcha = 100000 + secureRandom.nextInt(900000);
		LocalDateTime currentTime = LocalDateTime.now();
		String captchaCode = String.valueOf(captcha);
		captchaEntity entity = new captchaEntity();
		entity.setCaptchaCode(captchaCode);
		entity.setRegDate(new Timestamp(System.currentTimeMillis()));
		entity.setExpireTime(currentTime.plusMinutes(1));
		entity.setIsValidated(1);
		captchaEntity savedEntity = captchaRepository.save(entity);

		CaptchaDto captchaDto = new CaptchaDto();
		captchaDto.setCaptchaCode(savedEntity.getCaptchaCode());
		return captchaDto;
	}

	@Override
	@Transactional
	public boolean validateCaptcha(CaptchaDto request) {
		if (request == null || request.getCaptchaCode() == null || request.getCaptchaCode().trim().isEmpty()) {
			throw new CaptchaValidationException("Please enter the CAPTCHA code");
		}
		captchaEntity entity = captchaRepository.findByCaptchaCode(request.getCaptchaCode().trim())
				.orElseThrow(() -> new CaptchaValidationException("Invalid CAPTCHA code"));
		if (Integer.valueOf(2).equals(entity.getIsValidated())) {
			throw new CaptchaValidationException("CAPTCHA has already been verified");
		}
		
		if (entity.getExpireTime() == null || !entity.getExpireTime().isAfter(LocalDateTime.now())) {
			throw new CaptchaValidationException("CAPTCHA has expired");
		}
		
		entity.setIsValidated(2);
		entity.setRegDate(new Timestamp(System.currentTimeMillis()));
		captchaRepository.save(entity);
		
		return true;
	}

}
