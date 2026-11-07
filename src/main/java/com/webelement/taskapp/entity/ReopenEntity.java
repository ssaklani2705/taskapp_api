package com.webelement.taskapp.entity;

import java.time.LocalDateTime;

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "t_reopen")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReopenEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "i_reopenid")
	private Integer reopenId;

	@Column(name = "i_taskid")
	private Integer taskId;

	@Column(name = "i_assigntoid")
	private Integer assignedToId;

	@Column(name = "i_reopenbyid")
	private Integer reopenById;

	@Column(name = "d_reopendate")
	private LocalDateTime reopenDate;

	@Column(name = "s_description", columnDefinition = "LONGTEXT")
	private String description;

	@Column(name = "ts_regdate")
	private LocalDateTime regDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "i_taskid", referencedColumnName = "i_taskid", insertable = false, updatable = false)
	private TaskEntity task;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "i_assigntoid", referencedColumnName = "i_userid", insertable = false, updatable = false)
	private UserLoginEntity assignedToUser;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "i_reopenbyid", referencedColumnName = "i_userid", insertable = false, updatable = false)
	private UserLoginEntity reopenByUser;

}
