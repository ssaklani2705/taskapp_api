package com.webelement.taskapp.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.webelement.taskapp.dto.MailLogDTO;
import com.webelement.taskapp.entity.MailLogEntity;
import com.webelement.taskapp.repo.MailLogRepo;


@Service
public class MailLogService {

	@Value("${file_maillog:}")
	private String file_maillog;

	@Autowired
	MailLogRepo mailLogRepo;

	public Page<MailLogDTO> getMailLogDetails(int page, int size, String search) {
		Pageable pageable = PageRequest.of(page, size);
		return mailLogRepo.findMailLogDetails(pageable, search);
	}

	public String getMailHtmlById(int mailLogId) {
		Optional<MailLogEntity> optionalMailLog = mailLogRepo.findById(mailLogId);
		if (optionalMailLog.isPresent()) {
			MailLogEntity mailLog = optionalMailLog.get();
			String filename = mailLog.getFilename(); // only the file name, e.g. "mail123.html"

			try {
				// Combine configured base path + filename
				Path path = Paths.get(file_maillog, filename);

				// Read file (Java 8 compatible)
				byte[] bytes = Files.readAllBytes(path);
				return new String(bytes, StandardCharsets.UTF_8);

			} catch (IOException e) {
				e.printStackTrace();
				return "<p>Error reading file</p>";
			}
		} else {
			return "<p>Mail log not found</p>";
		}
	}

}