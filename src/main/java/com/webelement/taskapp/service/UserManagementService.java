package com.webelement.taskapp.service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.webelement.taskapp.common.CommonFunction;
import com.webelement.taskapp.common.ResponseApi;
import com.webelement.taskapp.dto.ModulePermissionDTO;
import com.webelement.taskapp.dto.UserActiveDTO;
import com.webelement.taskapp.dto.UserInfo;
import com.webelement.taskapp.entity.PermissionEntity;
import com.webelement.taskapp.entity.TransactionEntity;
import com.webelement.taskapp.entity.UserLoginEntity;
import com.webelement.taskapp.repo.PermissionRepo;
import com.webelement.taskapp.repo.UserLoginRepository;

@Service
public class UserManagementService {

	@Autowired
	private UserLoginRepository loginRepository;

	@Autowired
	private CommonFunction commonFunction;

	@Autowired
	private PermissionRepo permissionRepo;

	@Value("${paths:}")
	private String paths;

	@Value("${file_maillog:}")
	private String file_maillog;

	public List<UserActiveDTO> getActiveUsers() {
		return loginRepository.findActiveUsers();
	}

	public Page<UserInfo> findBasicUserInfo(int page, int size, int statusIndex, String search) {
		return loginRepository.findBasicUserInfo(PageRequest.of(page, size), statusIndex, search);
	}

	public ResponseEntity<ResponseApi<String>> deleteUser(int userId, int createdBy, HttpServletRequest httpRequest) {
		Optional<UserLoginEntity> existingUser = loginRepository.findById(userId);

		if (!existingUser.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseApi<>(false, "User not found", null));
		}

		int updatedRows = loginRepository.deleteUser(3, userId);

		if (updatedRows > 0) {
			commonFunction.createHistoryAccess(createdBy, commonFunction.resolveClientIp(httpRequest),
					commonFunction.getLocalIp(), "User Deleted", 1, userId, -1);
			return ResponseEntity.ok(new ResponseApi<>(true, "User deleted successfully", null));
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseApi<>(false, "Failed to delete user", null));
		}
	}

	public UserLoginEntity getUserById(int userId) {
		UserLoginEntity user = loginRepository.getUserById(userId);
		
		if (user != null) {

			int newUserId = 0;

			if ("N".equalsIgnoreCase(user.getPermission())) {
				newUserId = userId;
			} else {
				newUserId = -1;
			}
			List<ModulePermissionDTO> permissions = getPermissionsByUserId(newUserId);
			user.setModule(permissions);

			// Append transaction history
			List<TransactionEntity> history = getTransactionLogs(1, userId);
			user.setTransactionhistory(history);
		} else {
			int newUserId = (userId == 0) ? -1 : userId;
			
			List<ModulePermissionDTO> permissions = getPermissionsByUserId(newUserId);

			user = new UserLoginEntity(); // create new object
			user.setModule(permissions);
		}

		return user;
	}

	public List<TransactionEntity> getTransactionLogs(int moduleId, Integer recordId) {
		List<Object[]> results = loginRepository.getTransactionLogs(moduleId, recordId);
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

	public List<ModulePermissionDTO> getPermissionsByUserId(int userId) {

		List<Object[]> results = loginRepository.getModulePermissions(userId);

		return results.stream().map(obj -> {
			ModulePermissionDTO dto = new ModulePermissionDTO();

			dto.setUserId(obj[0] != null ? ((Number) obj[0]).intValue() : null);
			dto.setModuleId(obj[1] != null ? ((Number) obj[1]).intValue() : null);
			dto.setName(obj[2] != null ? obj[2].toString() : null);
			dto.setType(obj[3] != null ? ((Number) obj[3]).intValue() : null);

			dto.setAddPer(obj[4] != null ? obj[4].toString() : null);
			dto.setEditPer(obj[5] != null ? obj[5].toString() : null);
			dto.setDeletePer(obj[6] != null ? obj[6].toString() : null);
			dto.setApprovePer(obj[7] != null ? obj[7].toString() : null);
			dto.setAdminApprovePer(obj[8] != null ? obj[8].toString() : null);
			dto.setViewPer(obj[9] != null ? obj[9].toString() : null);
			dto.setExportExcel(obj[10] != null ? obj[10].toString() : null);

			return dto;
		}).collect(Collectors.toList());
	}

	public ResponseEntity<?> saveUserDetail(UserLoginEntity userRequest, HttpServletRequest httpRequest)
			throws Exception {
	
		boolean isEdit = userRequest.getUserId() > 0;

		if (!isEdit) {
			// ADD logic - check for duplicate email
			int isDuplicateEmail = loginRepository.checkDuplicacy(0, userRequest.getEmail());
			if (isDuplicateEmail == 1) {
				return ResponseEntity.status(HttpStatus.CONFLICT)
						.body(new ResponseApi<>(false, "User already exists", "Exist"));
			}
		} else {
			// EDIT logic - check if email belongs to another user
			int isDuplicateEmail = loginRepository.checkDuplicacy(userRequest.getUserId(), userRequest.getEmail());
			if (isDuplicateEmail == 1) {
				return ResponseEntity.status(HttpStatus.CONFLICT)
						.body(new ResponseApi<>(false, "Email already used by another user", "Exist"));
			}
		}

		int uid;
//		String password = "";
		if (!isEdit) {
			// Add new user
			// password = commonFunction.getRandomPassword(8);

			String fNameRaw = userRequest.getFirstName(); // e.g. "Sunil Saklani" or "Sunil"
			String fName = "";
			// Handle null / empty
			if (fNameRaw != null && !fNameRaw.trim().isEmpty()) {
				// Get only first word
				String firstWord = fNameRaw.trim().split("\\s+")[0];

				// Capitalize first letter, lower rest
				fName = firstWord.substring(0, 1).toUpperCase() + firstWord.substring(1).toLowerCase();

				// Append @123
				fName = fName + "@123";
			}

//			password = fName;

			uid = createUser(userRequest, userRequest.getPassword(), 0);

			// Optionally send welcome/reset email
//			String ip = commonFunction.resolveClientIp(httpRequest);
//			String subject = "HD APP :: Login Credentials";
//			int type = 1;

			commonFunction.createHistoryAccess(userRequest.getCreatedBy(), commonFunction.resolveClientIp(httpRequest),
					commonFunction.getLocalIp(), "Add User", 1, uid, -1);
		} else {
			// Update existing user
			uid = updateUser(userRequest);
			commonFunction.createHistoryAccess(userRequest.getCreatedBy(), commonFunction.resolveClientIp(httpRequest),
					commonFunction.getLocalIp(), "Update User", 1, uid, -1);
		}

		// Update permissions for both add & edit
		if (!"Y".equalsIgnoreCase(userRequest.getPermission()) && uid > 0) {
			permissionRepo.deleteByUserId(uid);
			if (userRequest.getModule() != null && !userRequest.getModule().isEmpty()) {
				for (ModulePermissionDTO m : userRequest.getModule()) {
					if ("Y".equalsIgnoreCase(m.getViewPer()) || "Y".equalsIgnoreCase(m.getAddPer())
							|| "Y".equalsIgnoreCase(m.getEditPer()) || "Y".equalsIgnoreCase(m.getDeletePer())
							|| "Y".equalsIgnoreCase(m.getApprovePer())
							|| "Y".equalsIgnoreCase(m.getAdminApprovePer()) || "Y".equalsIgnoreCase(m.getExportExcel())) {

						PermissionEntity entity = new PermissionEntity();
						entity.setUserId(uid);
						entity.setModuleId(m.getModuleId());
						entity.setAdd(defaultIfNull(m.getAddPer()));
						entity.setEdit(defaultIfNull(m.getEditPer()));
						entity.setDelete(defaultIfNull(m.getDeletePer()));
						entity.setApprove(defaultIfNull(m.getApprovePer()));
						entity.setAdminApprove(defaultIfNull(m.getAdminApprovePer()));
						entity.setView(defaultIfNull(m.getViewPer()));
						entity.setExportExcel(defaultIfNull(m.getExportExcel()));
						
						permissionRepo.save(entity);
					}
				}
			}
		}

		return ResponseEntity.ok(
				new ResponseApi<>(true, "Success", isEdit ? "User updated successfully" : "User added successfully"));
	}

	// Small helper to avoid null values
	private String defaultIfNull(String value) {
		return (value != null && !value.isEmpty()) ? value : "N";
	}

	// Create User
	public int createUser(UserLoginEntity info, String password, int loginuserId) {
		try {
			UserLoginEntity user = new UserLoginEntity();
			String rawName = info.getFirstName(); // e.g. "SUNIL SAKLANI"
			String formattedName = "";

			if (rawName != null && !rawName.trim().isEmpty()) {
				formattedName = Arrays.stream(rawName.trim().split("\\s+"))
						.map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
						.collect(Collectors.joining(" "));
			}

			user.setFirstName(formattedName);
			user.setMobileNo(info.getMobileNo());
			user.setEmail(info.getEmail());
			user.setPermission(info.getPermission());
			user.setPassword(commonFunction.cipher(password)); // keep your encryption logic
			user.setTelephone(info.getTelephone());
			user.setExpiryDate(info.getExpiryDate());
			user.setStatus((short) 1); // status is hardcoded as in JDBC
			String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			Timestamp timestamp = Timestamp.valueOf(dateStr);
			user.setRegDate(timestamp);
			user.setModDate(timestamp);
			user.setQcFlag((short) info.getQcFlag());
			user.setPcb(info.getPcb());
			user.setDepartmentId(info.getDepartmentId());
			UserLoginEntity savedUser = loginRepository.save(user);
			return savedUser.getUserId(); // JPA auto-fills the generated ID

		} catch (Exception e) {
			e.printStackTrace(); // or use a logger
			return 0;
		}
	}

	// Update User
	private int updateUser(UserLoginEntity request) throws Exception {
		// Fetch existing user, update only necessary fields
		UserLoginEntity existing = loginRepository.findById(request.getUserId())
				.orElseThrow(() -> new RuntimeException("User not found"));
		existing.setFirstName(request.getFirstName());
		existing.setMobileNo(request.getMobileNo());
		existing.setExpiryDate(request.getExpiryDate());
		existing.setStatus(request.getStatus());
		existing.setPermission(request.getPermission());
		existing.setTelephone(request.getTelephone());
		existing.setDepartmentId(request.getDepartmentId());
		// ✅ Update password only if provided
		if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
			String encodedPassword = commonFunction.cipher(request.getPassword());
			existing.setPassword(encodedPassword);
		}
		loginRepository.save(existing);
		return existing.getUserId();
	}



	@Transactional
	public int updateForgotpasswordlink(String username, String password, String linksentdate) throws Exception {
		Timestamp linkDateTs = linksentdate != null && !linksentdate.isEmpty() ? Timestamp.valueOf(linksentdate) : null;
		return loginRepository.updateForgotPasswordLink(username, commonFunction.cipher(password), linkDateTs);
	}

	

}
