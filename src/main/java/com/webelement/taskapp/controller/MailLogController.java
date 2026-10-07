package com.webelement.taskapp.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webelement.taskapp.dto.MailLogDTO;

import com.webelement.taskapp.service.MailLogService;

import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/mailLog")
@CrossOrigin(origins =  "${app.cors.allowed-origins}")
@RequiredArgsConstructor
public class MailLogController {
	private final MailLogService mailLogService;

	@GetMapping("/getMailLogDetails")
	public Map<String, Object> getMailLogDetails(@RequestParam int page, @RequestParam int size,
			@RequestParam String search) {
		Page<MailLogDTO> mailLogPage = mailLogService.getMailLogDetails(page, size, search);
		Map<String, Object> response = new HashMap<>();
		response.put("data", mailLogPage.getContent());
		response.put("totalElements", mailLogPage.getTotalElements());
		return response;
	}

	@GetMapping("/getMailLogHtml")
	public CompletableFuture<ResponseEntity<Map<String, String>>> getMailLogHtml(@RequestParam int mailLogId) {

		return mailLogService.getMailHtmlById(mailLogId).thenApply(htmlContent -> {

			Map<String, String> response = new HashMap<>();
			response.put("htmlContent", htmlContent);

			return ResponseEntity.ok(response);
		});
	}

	
}