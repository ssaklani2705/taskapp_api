package com.webelement.taskapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webelement.taskapp.Exceptions.ApiException;
import com.webelement.taskapp.common.CommonFunction;
import com.webelement.taskapp.dto.CompanyShiftDto;
import com.webelement.taskapp.entity.CompanyEntity;
import com.webelement.taskapp.entity.CompanyShiftEntity;
import com.webelement.taskapp.repo.CompanyRepo;
import com.webelement.taskapp.repo.CompanyShiftRepo;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
public class CompanyShiftService {

	private static final short DELETED = 3;
	private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");
	private static final String TIME_REGEX = "^([01]\\d|2[0-3]):[0-5]\\d$";
	private final HttpServletRequest httpRequest;
	private final CompanyShiftRepo repo;
	private final CommonFunction helpers;
	private final CompanyRepo companyRepo;

	private String validate(CompanyShiftDto d) {
		if (d.getCompanyId() == null || d.getCompanyId() <= 0)
			return "Company is required.";
		if (d.getShiftName() == null || d.getShiftName().trim().isEmpty())
			return "Shift name is required.";
		if (d.getFromTime() == null || !d.getFromTime().matches(TIME_REGEX))
			return "Valid from time (HH:mm) is required.";
		if (d.getToTime() == null || !d.getToTime().matches(TIME_REGEX))
			return "Valid to time (HH:mm) is required.";
		if (d.getFromTime().equals(d.getToTime()))
			return "From time and To time cannot be same.";
		return null;
	}

	private CompanyShiftDto toDto(CompanyShiftEntity e) {
		CompanyShiftDto d = new CompanyShiftDto();
		d.setShiftId(e.getShiftId());
		d.setShiftName(e.getShiftName());
		d.setFromTime(e.getFromTime() != null ? e.getFromTime().format(HHMM) : null);
		d.setToTime(e.getToTime() != null ? e.getToTime().format(HHMM) : null);
		d.setStatus(e.getStatus());
		d.setCompanyId(e.getCompanyId());
		return d;
	}

	@Transactional
	public Map<String, Object> add(CompanyShiftDto request) {
		String err = validate(request);
		if (err != null)
			return resp(false, err, null);
		
		LocalTime fromTime = LocalTime.parse(request.getFromTime());
		LocalTime toTime = LocalTime.parse(request.getToTime());
		
		//System.err.println(fromTime + " time  ================================ " + toTime);
		if (fromTime.equals(toTime)) {
		    throw ApiException.badRequest("From time and To time cannot be the same.");
		}
		if (fromTime.isAfter(toTime)) {
		    throw ApiException.badRequest("To time must be later than From time.");
		}
		if (repo.existsDuplicateName(request.getCompanyId(), request.getShiftName().trim(), 0))
			return resp(false, "Shift name already exists.", null);

		CompanyShiftEntity e = new CompanyShiftEntity();
		e.setShiftName(request.getShiftName().trim());
		e.setFromTime(LocalTime.parse(request.getFromTime()));
		e.setToTime(LocalTime.parse(request.getToTime()));
		e.setCompanyId(request.getCompanyId());
		e.setStatus((short) 1);
		e.setRegistrationDate(LocalDateTime.now());
		repo.save(e);
		
		String action = "Shift \"" + e.getShiftName() + "\" has been added.";
		helpers.createHistoryAccess(request.getUserId(), helpers.resolveClientIp(httpRequest), helpers.getLocalIp(),action, 16, request.getCompanyId(), -1);
		return resp(true, "Shift added successfully.", null);
	}

	@Transactional
	public Map<String, Object> update(CompanyShiftDto d) {
		if (d.getShiftId() == null || d.getShiftId() <= 0)
			return resp(false, "Shift id is required.", null);
		String err = validate(d);
		if (err != null)
			return resp(false, err, null);

		Optional<CompanyShiftEntity> opt = repo.findByShiftIdAndStatusNot(d.getShiftId(), DELETED);
		if (opt.isEmpty() || !opt.get().getCompanyId().equals(d.getCompanyId()))
			return resp(false, "Shift not found.", null);
		if (repo.existsDuplicateName(d.getCompanyId(), d.getShiftName().trim(), d.getShiftId()))
			return resp(false, "Shift name already exists.", null);

		CompanyShiftEntity e = opt.get();
		e.setShiftName(d.getShiftName().trim());
		e.setFromTime(LocalTime.parse(d.getFromTime()));
		e.setToTime(LocalTime.parse(d.getToTime()));
		e.setModificationDate(LocalDateTime.now());
		repo.save(e);
		String action = "Shift \"" + e.getShiftName() + "\" has been updated.";
		
		helpers.createHistoryAccess(d.getUserId(), helpers.resolveClientIp(httpRequest), helpers.getLocalIp(), action,
				16, d.getCompanyId(), -1);
		return resp(true, "Shift updated successfully.", null);
	}

	@Transactional
	public Map<String, Object> delete(Integer shiftId,Integer userId) {
		Optional<CompanyShiftEntity> opt = repo.findByShiftIdAndStatusNot(shiftId, DELETED);
		if (opt.isEmpty())
			return resp(false, "Shift not found.", null);

		CompanyShiftEntity e = opt.get();
		e.setStatus(DELETED); // soft delete
		e.setModificationDate(LocalDateTime.now());
		repo.save(e);
		String action = "Shift \"" + e.getShiftName() + "\" has been deleted.";
		
		helpers.createHistoryAccess(userId, helpers.resolveClientIp(httpRequest), helpers.getLocalIp(), action, 16,
				opt.get().getCompanyId(), -1);
		return resp(true, "Shift deleted successfully.", null);
	}

	public Map<String, Object> viewById(Integer shiftId) {
		return repo.findByShiftIdAndStatusNot(shiftId, DELETED).map(e -> resp(true, "OK", toDto(e)))
				.orElseGet(() -> resp(false, "Shift not found.", null));
	}

	public Map<String, Object> viewList(Integer companyId) {
		List<CompanyShiftDto> list = repo.findByCompanyIdAndStatusNotOrderByFromTimeAscShiftIdAsc(companyId, DELETED)
				.stream().map(this::toDto).collect(Collectors.toList());
		return resp(true, "OK", list);
	}

	private Map<String, Object> resp(boolean success, String message, Object data) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("success", success);
		m.put("message", message);
		m.put("data", data);
		return m;
	}
}
