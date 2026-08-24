package com.webelement.taskapp.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webelement.taskapp.dto.MailLogDTO;
import com.webelement.taskapp.entity.MailLogEntity;


public interface MailLogRepo extends JpaRepository<MailLogEntity, Integer> {

	@Query("SELECT new com.webelement.taskapp.dto.MailLogDTO("
			+ "m.mailLogId, m.type, m.name, m.to, m.cc, m.bcc, m.from, m.subject, "
			+ "m.filename, m.moduleId, m.regDate, m.modDate, m.ipAddress, m.localIp, m.status) "
			+ "FROM MailLogEntity m " + "WHERE (" + "  :search IS NULL OR :search = '' OR "
			+ "  LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ "  LOWER(m.subject) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ "  LOWER(m.to) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ "  LOWER(m.from) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ "  LOWER(m.cc) LIKE LOWER(CONCAT('%', :search, '%'))" + ") " + "ORDER BY m.regDate DESC")
	Page<MailLogDTO> findMailLogDetails(Pageable pageable, @Param("search") String search);

}
