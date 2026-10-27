package com.webelement.taskapp.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webelement.taskapp.entity.CompanyShiftEntity;

public interface CompanyShiftRepo extends JpaRepository<CompanyShiftEntity, Integer>{

	 // status 3 = deleted
    List<CompanyShiftEntity> findByCompanyIdAndStatusNotOrderByFromTimeAscShiftIdAsc(Integer companyId, Short status);

    Optional<CompanyShiftEntity> findByShiftIdAndStatusNot(Integer shiftId, Short status);

    @Query("SELECT COUNT(s) > 0 FROM CompanyShiftEntity s " +
           "WHERE s.companyId = :companyId AND LOWER(s.shiftName) = LOWER(:name) " +
           "AND s.status <> 3 AND s.shiftId <> :excludeId")
    boolean existsDuplicateName(@Param("companyId") Integer companyId,
                                @Param("name") String name,
                                @Param("excludeId") Integer excludeId);
}
