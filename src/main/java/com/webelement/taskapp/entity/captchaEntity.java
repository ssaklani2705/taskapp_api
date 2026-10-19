package com.webelement.taskapp.entity;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@Setter
@Getter
@ToString
@Entity
@Table(name = "t_captcha")
public class captchaEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "i_captchaid")
	private Integer captchaId;

	@Column(name = "s_captchacode")
	private String captchaCode;

	@Column(name = "ts_regdate")
	private Timestamp regDate;

	@Column(name = "is_validated", nullable = false, columnDefinition = "INT DEFAULT 1")
	private Integer isValidated = 1;
	@Column(name = "ts_expiretime")
	private LocalDateTime expireTime;
}
