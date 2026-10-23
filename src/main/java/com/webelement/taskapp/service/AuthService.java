package com.webelement.taskapp.service;
import java.util.List;
import org.springframework.http.ResponseEntity;
import com.webelement.taskapp.common.ResponseApi;
import com.webelement.taskapp.dto.LoginRequest;
import com.webelement.taskapp.dto.LoginResponse;
import com.webelement.taskapp.dto.ModulePermissionDTO;

public interface AuthService {

	 	public ResponseEntity<ResponseApi<LoginResponse>> login(LoginRequest request) throws Exception;

	    public ResponseEntity<ResponseApi<String>> forgotpasswordMail(String emailId, Boolean isManager, String loginType) throws Exception;

	    public ResponseEntity<ResponseApi<String>> forgotpassword(String emailId, String userId, String password,String isManager) throws Exception;

	    public List<ModulePermissionDTO> getModuleListByName(int userId, boolean isFullList);

	    public int updateForgotpasswordlink(String username, String password, String linksentdate) throws Exception;

	    public int checkURLValidity(String email, int userId);

		public int getLoginIdMatch(String userName, String password) throws Exception;

}
