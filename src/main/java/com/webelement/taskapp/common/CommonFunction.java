package com.webelement.taskapp.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.webelement.taskapp.entity.TransactionEntity;
import com.webelement.taskapp.repo.TransactionRepo;
import com.webelement.taskapp.repo.UserLoginRepository;

@Component
public class CommonFunction {
	
	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	TransactionRepo transactionRepo;
	@Autowired
	private UserLoginRepository userLoginRepository;

	@Value("${ipflag:}")
	private String ipFlag;

	@Value("${algorithm:}")
	private String ALGORITHM;

	@Value("${secret_key:}")
	private String SECRET_KEY;
	
	@Value("${pdffilepath}")
    private String templatePath;
	
	public List<TransactionEntity> getTransactionLogs(int moduleId, Integer recordId) {
		List<Object[]> results = userLoginRepository.getTransactionLogs(moduleId, recordId);
		System.out.println("results : " + results);
		return results.stream().map(obj -> {
			TransactionEntity dto = new TransactionEntity();
			dto.setEntryDate((String) obj[0]);
			dto.setName((String) obj[1]);
			dto.setAction((String) obj[2]);
			dto.setUserId(obj[3] != null ? ((Number) obj[3]).intValue() : null);
			dto.setFlag((String) obj[4]);
			return dto;
		}).collect(Collectors.toList());
	}
	
	public void createHistoryAccess(int userId, String ipAddrStr, String localip, String desc, int moduleId,
			int recordid, int bankUserId) {
		TransactionEntity entity = new TransactionEntity();
		entity.setModuleId(moduleId);
		entity.setRecordId(recordid);
		entity.setUserId(userId);
		entity.setIpAddress(ipAddrStr);
		entity.setLocalIp(localip);
		entity.setAction(desc);
		entity.setRegDate(new Timestamp(System.currentTimeMillis())); // current time
		entity.setBankUserId(bankUserId);
		transactionRepo.save(entity);
	}
	
	public String getLocalIp() {
		try {
			InetAddress localHost = InetAddress.getLocalHost();
			return localHost.getHostAddress();
		} catch (Exception e) {
			e.printStackTrace();
			return "UNKNOWN";
		}
	}

	public String decipher(String data) throws Exception {
		if (SECRET_KEY == null || SECRET_KEY.length() != 8) {
			throw new Exception("Invalid key length - 8 bytes key needed!");
		}
		SecretKey key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, key);
		return new String(cipher.doFinal(toByte(data)));
	}

	// Converts hex string to byte array
	private static byte[] toByte(String hexString) {
		int len = hexString.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
					+ Character.digit(hexString.charAt(i + 1), 16));
		}
		return data;
	}

	public String cipher(String data) throws Exception {

		if (SECRET_KEY == null || SECRET_KEY.length() != 8) {
			throw new IllegalArgumentException("Invalid key length - 8 bytes key needed!");
		}
		SecretKey key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] encryptedBytes = cipher.doFinal(data.getBytes());
		return toHex(encryptedBytes);
	}

	// Converts byte array to hex string
	private static String toHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02X", b));
		}
		return sb.toString();
	}

	public String resolveClientIp(HttpServletRequest request) {
		String ipAddrStr = "";
		String iplocalserver = ipFlag;

		try {
			if ("localIp".equalsIgnoreCase(iplocalserver)) {
				// Get local server IP address
				InetAddress addr = InetAddress.getLocalHost();
				ipAddrStr = addr.getHostAddress();
			} else {
				// Try to get real client IP from headers (in case of proxy or load balancer)
				ipAddrStr = request.getHeader("X-FORWARDED-FOR");

				// Fallback to remote address
				if (ipAddrStr == null || ipAddrStr.isEmpty()) {
					ipAddrStr = request.getRemoteAddr();
				}
			}
		} catch (Exception e) {
			ipAddrStr = "UNKNOWN";
		}
		return ipAddrStr;
	}
	
	public String getForgotMessageCreate(String name, String link, String url) {
		try {
			// Load the HTML template from resources/templates/
			Resource resource = resourceLoader.getResource("classpath:templates/newuser_admin.html");
			String content = new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);
			content = content.replace("__NAME__", name);
			content = content.replace("__LINK__", link);
			content = content.replace("__URL__", url); // Optional
			return content;
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public String createFolder(String path) {
		String foldername = "";
		try {
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Calcutta"));
			int month = (cal.get(Calendar.MONTH) + 1);
			int year = cal.get(Calendar.YEAR);
			SimpleDateFormat sdf1 = new SimpleDateFormat("M");
			SimpleDateFormat sdf2 = new SimpleDateFormat("MMM");
			String monthName = sdf2.format(sdf1.parse(month + ""));

			foldername = (monthName + "-" + year).toLowerCase();
			File dir = new File(path + foldername + "/");
//	            if (!dir.exists()) {
//	                dir.mkdir();
//	            }
			if (!dir.exists()) {
				dir.mkdirs(); // This is safer
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return foldername;
	}
	
	public String writeHTMLFile(String content, String filePath, String fileName) {
		try {
			File dir = new File(filePath);
			if (!dir.exists()) {
				dir.mkdirs(); // Use mkdirs() to ensure parent directories are also created
			}
			fileName = fileName + ".html";
			File file = new File(dir, fileName); // Cleaner path handling

			try (BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
				writer.write(content);
			}
			return fileName;
		} catch (Exception e) {
			e.printStackTrace(); // You could use a logger instead
			return null;
		}
	}
	
	public String currDate1() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Calcutta"));
		java.util.Date dt = cal.getTime();
		return sdf.format(dt);
	}
	
	public static String getDateAfter(String date, int type, int no, String sformatv, String endformatv) {
		String dt = "";
		try {
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sformat = new SimpleDateFormat(sformatv);
			SimpleDateFormat endformat = new SimpleDateFormat(endformatv);
			long l1 = sformat.parse(date).getTime();
			cal.setTimeInMillis(l1);
			switch (type) {
			case 1:
				cal.add(Calendar.DATE, no);
				break;
			case 2:
				cal.add(Calendar.MONTH, no);
				break;
			case 3:
				cal.add(Calendar.YEAR, no);
				break;
			case 4:
				cal.add(Calendar.HOUR, no);
				break;
			default:
				break;
			}
			dt = endformat.format(cal.getTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dt;
	}
}
