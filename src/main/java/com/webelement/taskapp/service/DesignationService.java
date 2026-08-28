package com.webelement.taskapp.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.webelement.taskapp.common.CommonFunction;
import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.DesignationDTO;
import com.webelement.taskapp.entity.DesignationEntity;
import com.webelement.taskapp.entity.TransactionEntity;
import com.webelement.taskapp.repo.DesignationRepository;
import com.webelement.taskapp.repo.UserLoginRepository;

@Service
public class DesignationService {

    @Autowired
    private DesignationRepository designationRepository;

    @Autowired
    private CommonFunction commonFunction;
    
	@Autowired
	private UserLoginRepository userLoginRepository;

	// ----------------------------------------------------
	// ADD / UPDATE
	// ----------------------------------------------------

	public ApiResponse<DesignationDTO> addOrUpdate(DesignationDTO dto, HttpServletRequest httpRequest) {

		Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());

		String name = dto.getName() != null ? dto.getName().trim() : "";

		// ADD
		if (dto.getDesigmationId() == null || dto.getDesigmationId() == 0) {

			if (designationRepository.existsByNameIgnoreCaseAndStatusNot(name, 3)) {

				return new ApiResponse<>(false, "Designation name already exists", null);
			}
			
			if (designationRepository.existsBySequenceAndStatusNot(dto.getSequence(), 3)) {
					
				return new ApiResponse<>(false, "A designation with this sequence already exists", null);
			}

		}

		// UPDATE
		else {

			DesignationEntity existing = designationRepository.findByNameIgnoreCase(name);

			if (existing != null && !existing.getDesignationId().equals(dto.getDesigmationId())
					&& existing.getStatus() != 3) {

				return new ApiResponse<>(false, "A designation with this sequence already exists", null);
			}

			DesignationEntity existingSequence = designationRepository.findBySequence(dto.getSequence());

			if (existingSequence != null && !existingSequence.getDesignationId().equals(dto.getDesigmationId())
					&& existingSequence.getStatus() != 3) {

				return new ApiResponse<>(false, "A designation with this sequence already exists", null);
			}
		}

		DesignationEntity entity;

		// Existing record
		if (dto.getDesigmationId() != null && dto.getDesigmationId() != 0) {

			entity = designationRepository.findById(dto.getDesigmationId())
					.orElseThrow(() -> new RuntimeException("Designation Record not found"));

			entity.setName(name);
			entity.setSequence(dto.getSequence());
			entity.setUserId(dto.getUserId());

			if (dto.getStatus() != null) {
				entity.setStatus(dto.getStatus());
			}

			entity.setModdate(timestamp);
		}

		// New record
		else {

			entity = new DesignationEntity();

			entity.setName(name);
			entity.setSequence(dto.getSequence() != null ? dto.getSequence() : 0);

			entity.setUserId(dto.getUserId());
			entity.setStatus(1);
			entity.setRegdate(timestamp);
		}

		DesignationEntity saved = designationRepository.save(entity);

		DesignationDTO responseDTO = new DesignationDTO();

		responseDTO.setDesigmationId(saved.getDesignationId());

		responseDTO.setName(saved.getName());

		responseDTO.setSequence(saved.getSequence());

		responseDTO.setUserId(saved.getUserId());

		responseDTO.setStatus(saved.getStatus());

		responseDTO.setRegdate(saved.getRegdate());

		responseDTO.setModdate(saved.getModdate());

		boolean isNew = dto.getDesigmationId() == null || dto.getDesigmationId() == 0;

		String action = isNew ? "Designation Added" : "Designation Updated";

		commonFunction.createHistoryAccess(dto.getUserId(), commonFunction.resolveClientIp(httpRequest),
				commonFunction.getLocalIp(), action, 3, saved.getDesignationId(), -1);

		return new ApiResponse<>(true, "Designation saved successfully", responseDTO);
	}

    // ----------------------------------------------------
    // GET BY ID
    // ----------------------------------------------------

    public ApiResponse<DesignationDTO> getById(
            Integer id) {

        DesignationEntity entity =
                designationRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Designation Record not found"));

        DesignationDTO dto =
                new DesignationDTO();

        dto.setDesigmationId(
                entity.getDesignationId());

        dto.setName(
                entity.getName());

        dto.setSequence(
                entity.getSequence());

        dto.setUserId(
                entity.getUserId());

        dto.setStatus(
                entity.getStatus());

        dto.setRegdate(
                entity.getRegdate());

        dto.setModdate(
                entity.getModdate());

        List<TransactionEntity> history =
                commonFunction.getTransactionLogs(
                        3,
                        id);

        if (history != null && !history.isEmpty()) {
            dto.setTransactionHistory(history);
        }

        return new ApiResponse<>(
                true,
                "Designation fetched successfully",
                dto);
    }

    // ----------------------------------------------------
    // PAGINATION + SEARCH
    // ----------------------------------------------------

    public Page<DesignationDTO> findDesigmationDetails(
            int page,
            int size,
            int statusIndex,
            String search) {

        return designationRepository.findDesignationDetails(
                PageRequest.of(page, size),
                statusIndex,
                search);
    }

    // ----------------------------------------------------
    // DELETE
    // ----------------------------------------------------

    public ResponseEntity<ApiResponse<String>> deleteDesigmation(
            Integer desigmationId,
            Integer userId,
            HttpServletRequest httpRequest) {

        Optional<DesignationEntity> existing =
                designationRepository.findById(
                        desigmationId);

        if (!existing.isPresent()) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            new ApiResponse<>(
                                    false,
                                    "Designation not found",
                                    null));
        }
        
        
        // -----------------------------------------------------
        // CHECK IF DESIGNATION IS IN USE (any status - active/inactive)
        // -----------------------------------------------------
        boolean isDesignationInUse = userLoginRepository.existsByDesignationId(desigmationId);
        if (isDesignationInUse) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(false, "Designation is assigned to one or more users and cannot be deleted", null));
        }


        int updatedRows =
                designationRepository.softDelete(
                        3,
                        desigmationId);

        if (updatedRows > 0) {

            commonFunction.createHistoryAccess(
                    userId,
                    commonFunction.resolveClientIp(httpRequest),
                    commonFunction.getLocalIp(),
                    "Designation Deleted",
                    3,
                    desigmationId,
                    -1);

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            "Designation deleted successfully",
                            null));
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        new ApiResponse<>(
                                false,
                                "Failed to delete designation",
                                null));
    }

    // ----------------------------------------------------
    // ACTIVE LIST
    // ----------------------------------------------------

    public List<DesignationDTO> getActiveDesigmations() {

        List<DesignationEntity> list =
                designationRepository.findByStatus(1);

        return list.stream()
                .map(d ->
                        new DesignationDTO(
                                d.getDesignationId(),
                                d.getName()))
                .collect(Collectors.toList());
    }
}
