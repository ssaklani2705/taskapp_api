package com.webelement.taskapp.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webelement.taskapp.dto.MailLogDTO;
import com.webelement.taskapp.entity.MailLogEntity;
import com.webelement.taskapp.service.MailLogService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/mailLog")
@CrossOrigin(origins = { "http://localhost:4500", "https://www.iba.org.in", "https://13.202.30.190" })
public class MailLogController {

	@Autowired
	MailLogService mailLogService;

	@Value("${task.upload-dir}") // change to your actual property key
	private String uploadDir;

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

	@GetMapping("/download")
	public ResponseEntity<Resource> download(@RequestParam String type, @RequestParam String fileName)
			throws IOException {

		Path base = Paths.get(uploadDir, type).toAbsolutePath().normalize();
		Path filePath = base.resolve(fileName).normalize();

		System.err.println("Looking for: " + filePath + " exists=" + Files.exists(filePath));
		// Prevent path traversal
		if (!filePath.startsWith(base) || !Files.exists(filePath)) {
			return ResponseEntity.notFound().build();
		}

		Resource resource = new UrlResource(filePath.toUri());

		String contentType = Files.probeContentType(filePath);
		if (contentType == null) {
			contentType = "application/octet-stream";
		}

		return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
				.contentLength(Files.size(filePath))
				.header(HttpHeaders.CONTENT_DISPOSITION,
						ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build().toString())
				.body(resource);
	}

}