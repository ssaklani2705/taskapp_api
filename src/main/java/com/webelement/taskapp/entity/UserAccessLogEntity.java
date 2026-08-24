package com.webelement.taskapp.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import javax.persistence.*;

@Entity
@Table(name = "t_user_access_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserAccessLogEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "i_logid")
	private Integer logId;

	@Column(name = "i_userid")
	private Integer userId;

	@Column(name = "s_ipaddress")
	private String ipAddress;

	@Column(name = "ts_logintime")
	private LocalDateTime loginTime;

	@Column(name = "ts_logouttime")
	private LocalDateTime logoutTime;

	@Column(name = "i_status")
	private Integer status;

	@Column(name = "s_username")
	private String userName;

	@Column(name = "i_bankuserid")
	private Integer ibankUserId;

}
