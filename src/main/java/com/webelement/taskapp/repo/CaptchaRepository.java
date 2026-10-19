package com.webelement.taskapp.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.webelement.taskapp.entity.captchaEntity;
@Repository
public interface CaptchaRepository extends JpaRepository<captchaEntity, Long> {
//	Optional<captchaEntity> findByCaptchaCode(String captchaCode);

	Optional<captchaEntity> findByCaptchaId(Integer captchaId);

	@Query(value = "SELECT i_captchaid,s_captchacode,is_validated,ts_regdate,ts_expiretime FROM t_captcha WHERE s_captchacode = :captchaCode AND is_validated = 1  LIMIT 1", nativeQuery = true)
	Optional<captchaEntity> findByCaptchaCode(@Param("captchaCode") String captchaCode);
}
