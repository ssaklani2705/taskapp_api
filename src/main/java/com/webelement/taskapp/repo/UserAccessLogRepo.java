package com.webelement.taskapp.repo;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webelement.taskapp.dto.UserAccessLogDTO;
import com.webelement.taskapp.entity.UserAccessLogEntity;


public interface UserAccessLogRepo extends JpaRepository<UserAccessLogEntity, Integer> {

	Optional<UserAccessLogEntity> findByUserIdAndStatus(Integer userId, Integer status);

	Optional<UserAccessLogEntity> findByLogId(Integer logId);

	@Query("SELECT new com.webelement.taskapp.dto.UserAccessLogDTO("
			+ "u.logId, u.userId, u.ipAddress, u.loginTime, u.logoutTime, " + "u.status, u.userName) "
			+ "FROM UserAccessLogEntity u " + " WHERE (:search IS NULL OR :search = '' OR "
			+ "LOWER(u.ipAddress) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ "LOWER(u.userName) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ "CAST(u.loginTime AS string) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ "CAST(u.logoutTime AS string) LIKE LOWER(CONCAT('%', :search, '%'))) " + "ORDER BY u.loginTime DESC")
	Page<UserAccessLogDTO> findUserAccessDetails(Pageable pageable, @Param("search") String search);

}
