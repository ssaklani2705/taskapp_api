package com.webelement.taskapp.service;

import java.nio.file.AccessDeniedException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.webelement.taskapp.entity.UserLoginEntity;
import com.webelement.taskapp.jwttoken.AppUser;
import com.webelement.taskapp.jwttoken.CurrentUser;
import com.webelement.taskapp.repo.TaskRepository;
import com.webelement.taskapp.repo.UserLoginRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TaskRepository taskRepo;                       // whichever repository you put the query in
    private final UserLoginRepository userLoginRepository;

    public List<Map<String, Object>> workloadPerUser(LocalDate from, LocalDate to, List<Integer> requestedCatIds) {
        AppUser me = CurrentUser.get();
        if (!me.isAdmin() && !me.isHod()) {
            throw new org.springframework.security.access.AccessDeniedException("Forbidden");
        }
        if (to.isBefore(from) || ChronoUnit.DAYS.between(from, to) > 366) {
            throw new IllegalArgumentException("Choose a valid date range (maximum one year).");
        }

        List<Integer> requested = requestedCatIds == null ? Collections.emptyList() : requestedCatIds;

        int allCats = 1;
        List<Integer> catIds = Collections.singletonList(-1);     // placeholder, never used when allCats = 1

        if (!me.isAdmin()) {                                        // HOD: only their own categories
            UserLoginEntity user = userLoginRepository.findById(me.getUserId()).orElseThrow();
            List<Integer> mine = new ArrayList<>();
            if (user.getTaskcategoryIds() != null) {
                for (String s : user.getTaskcategoryIds().split(",")) {
                    if (!s.trim().isEmpty()) mine.add(Integer.parseInt(s.trim()));
                }
            }

            List<Integer> allowed = requested.isEmpty()
                    ? mine
                    : mine.stream().filter(requested::contains).collect(Collectors.toList());

            allCats = 0;
            catIds = allowed.isEmpty() ? Collections.singletonList(-1) : allowed;

        } else if (!requested.isEmpty()) {                          // admin: only the selected categories
            allCats = 0;
            catIds = requested;
        }

        return taskRepo.findWorkloadPerUser(
                java.sql.Timestamp.valueOf(from.atStartOfDay()),
                java.sql.Timestamp.valueOf(to.plusDays(1).atStartOfDay()),
                allCats, catIds);
    }
}
