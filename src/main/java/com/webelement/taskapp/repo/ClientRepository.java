package com.webelement.taskapp.repo;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.webelement.taskapp.dto.ClientDTO;
import com.webelement.taskapp.entity.ClientEntity;

@Repository
public interface ClientRepository extends JpaRepository<ClientEntity, Integer> {

	boolean existsByCode(String code);

	boolean existsByCodeAndClientIdNot(String code, Integer clientId);

	@Query("SELECT new com.webelement.taskapp.dto.ClientDTO("
			+ "c.clientId, c.name, c.code, c.pan, c.status, c.gstFlag, c.gstNo, c.stateId, s.name, c.addressLine1, c.addressLine2, "
			+ "c.city, c.pincode, c.contactName, c.contactEmail, c.emails, c.startDate, c.monthlyCharge, c.outstanding, "
			+ "c.name1, c.emailId1, c.name2, c.emailId2, c.name3, c.emailId3, c.managerId, u.firstName, c.userId, "
			+ "c.regdate, c.moddate, c.taxFlag, c.location) " + " FROM ClientEntity c LEFT JOIN StateEntity s "
			+ "ON c.stateId = s.stateId LEFT JOIN UserLoginEntity u ON c.managerId = u.userId WHERE c.clientId = :clientId")
	ClientDTO getClientById(@Param("clientId") int clientId);

	@Query("SELECT new com.webelement.taskapp.dto.ClientDTO("
			+ "c.clientId, c.name, c.code, c.pan, c.status, c.gstFlag, c.gstNo, "
			+ "c.stateId, s.name, c.addressLine1, c.addressLine2, "
			+ "c.city, c.pincode, c.contactName, c.contactEmail, c.emails, "
			+ "c.startDate, c.monthlyCharge, c.outstanding, "
			+ "c.name1, c.emailId1, c.name2, c.emailId2, c.name3, c.emailId3, "
			+ "c.managerId, u.firstName, c.userId, c.regdate, c.moddate, " + "c.taxFlag, c.location) "
			+ "FROM ClientEntity c " + "LEFT JOIN StateEntity s ON c.stateId = s.stateId "
			+ "LEFT JOIN UserLoginEntity u ON c.managerId = u.userId " + "WHERE c.clientId > 0 "
			+ "AND (:status IS NULL OR c.status = :status) " + "AND (:managerId IS NULL OR c.managerId = :managerId) "
			+ "AND (:stateId IS NULL OR c.stateId = :stateId) " + "AND (:clientName IS NULL OR :clientName = '' "
			+ "OR LOWER(c.name) LIKE LOWER(CONCAT('%', :clientName, '%'))) "
			+ "AND (:clientCode IS NULL OR :clientCode = '' "
			+ "OR LOWER(c.code) LIKE LOWER(CONCAT('%', :clientCode, '%'))) "
			+ "AND (:contactName IS NULL OR :contactName = '' "
			+ "OR LOWER(c.contactName) LIKE LOWER(CONCAT('%', :contactName, '%'))) "
			+ "AND (:contactEmail IS NULL OR :contactEmail = '' "
			+ "OR LOWER(c.contactEmail) LIKE LOWER(CONCAT('%', :contactEmail, '%'))) "
			+ "AND (:search IS NULL OR :search = '' " + "OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) "
			+ "OR LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%')) "
			+ "OR LOWER(c.contactName) LIKE LOWER(CONCAT('%', :search, '%')) "
			+ "OR LOWER(c.contactEmail) LIKE LOWER(CONCAT('%', :search, '%')))")
	Page<ClientDTO> findClientDetails(Pageable pageable, @Param("status") Short status,
			@Param("managerId") Integer managerId, @Param("stateId") Integer stateId,
			@Param("clientName") String clientName, @Param("clientCode") String clientCode,
			@Param("contactName") String contactName, @Param("contactEmail") String contactEmail,
			@Param("search") String search);

	@Modifying
	@Query("UPDATE ClientEntity u " + "SET u.status = :status " + "WHERE u.clientId = :clientId")
	int deleteClient(@Param("status") Short status, @Param("clientId") Integer clientId);

	@Query("SELECT LOWER(c.name) FROM ClientEntity c WHERE LOWER(c.name) IN :names AND c.status IN (1, 2)")
	List<String> findExistingNames(@Param("names") List<String> names);

	@Query("SELECT LOWER(c.contactEmail) FROM ClientEntity c WHERE LOWER(c.contactEmail) IN :emails AND c.status IN (1, 2)")
	List<String> findExistingEmails(@Param("emails") List<String> emails);

	@Query("SELECT LOWER(c.code) FROM ClientEntity c WHERE LOWER(c.code) IN :code AND c.status IN (1, 2)")
	List<String> findExistingCodes(@Param("code") List<String> code);

	@Query("SELECT UPPER(TRIM(c.gstNo)) FROM ClientEntity c WHERE c.gstNo IS NOT NULL")
	Set<String> findAllGstsNormalized();
	
	List<ClientEntity> findByManagerIdAndStatus(Integer managerId, Short status);
	
	Optional<ClientEntity> findByClientIdAndManagerIdAndStatus( Integer clientId, Integer managerId, Short status );

	Optional<ClientEntity> findByClientIdAndManagerId(Integer clientId, Integer userId);
	
	@Query("SELECT c FROM ClientEntity c WHERE c.status = 1 ORDER BY c.name ASC")
	List<ClientEntity> findAllActiveClients();
}
