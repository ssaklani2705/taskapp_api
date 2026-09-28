package com.webelement.taskapp.controller;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.webelement.taskapp.Exceptions.FileDownloadException;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
@CrossOrigin(origins = { "http://localhost:4500", "https://www.iba.org.in", "https://13.202.30.190" })
public class FileController {
	@Value("${task.upload-dir}") 
	private String uploadDir;

	@Qualifier("fileExecutor")
	private final Executor fileExecutor;

	@GetMapping("/download")
	public CompletableFuture<ResponseEntity<byte[]>> download(
	        @RequestParam String type,
	        @RequestParam String fileName) {

	    return CompletableFuture.supplyAsync(() -> {
	        try {
	            return buildResponse(type, fileName);
	        } catch (IOException e) {
	            throw new FileDownloadException(
	                    "Error downloading file.", e);
	        }
	    }, fileExecutor);
	}

	private ResponseEntity<byte[]> buildResponse(String type, String fileName) throws IOException {

		Path base = Paths.get(uploadDir, type).toAbsolutePath().normalize();

		Path filePath = base.resolve(fileName).normalize();

		if (!filePath.startsWith(base)) {
			throw new FileDownloadException("Invalid file path.");
		}

		if (!Files.exists(filePath)) {
			throw new FileDownloadException("File not found : " + fileName);
		}

		byte[] fileBytes = Files.readAllBytes(filePath);

		String contentType = Files.probeContentType(filePath);

		if (contentType == null) {
			contentType = "application/octet-stream";
		}

		return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).contentLength(fileBytes.length)
				.header(HttpHeaders.CONTENT_DISPOSITION,
						ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build().toString())
				.body(fileBytes);
	}

}
