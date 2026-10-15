package com.webelement.taskapp.dto;


import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@Getter
@Setter
@NoArgsConstructor

@ToString
public class LoginResponse {
	private String token;
	private int userId;
	private String username;
	private String refreshToken;
	private int sessionId;
	private String isAdmin;
	private String designationName;
	private String isHod;
	
	private List<ModulePermissionDTO> modules;
	private String emailId;
	
	// Added category IDs
    private List<Integer> categoryIds;

    public LoginResponse(
            String token,
            int userId,
            String username,
            Integer sessionId,
            String isAdmin,
            String designationName,
            List<ModulePermissionDTO> modules,
            String isHod,
            String emailId,
            List<Integer> categoryIds) {

        this.token = token;
        this.userId = userId;
        this.username = username;
        this.sessionId = sessionId != null ? sessionId : 0;
        this.isAdmin = isAdmin;
        this.designationName = designationName;
        this.modules = modules;
        this.isHod = isHod;
        this.emailId = emailId;
        this.categoryIds = categoryIds;
    }

	

}