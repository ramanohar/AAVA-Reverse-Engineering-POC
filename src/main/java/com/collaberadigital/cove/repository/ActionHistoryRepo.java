package com.collaberadigital.cove.repository;

import com.collaberadigital.cove.model.entity.ActionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ActionHistoryRepo extends JpaRepository<ActionHistory,Long> {

    @Query("SELECT u FROM ActionHistory u WHERE " +
            "u.action IN (:statuses) AND " +
            "u.userRole IN (:roles) AND " +
            "(u.userCompany LIKE CONCAT('%', :company, '%')) AND " +
            "(u.userName LIKE CONCAT('%', :name, '%') OR u.updateByAdminName LIKE CONCAT('%', :name, '%')) AND " +
            "(u.userEmail LIKE CONCAT('%', :email, '%') OR u.updateByAdminEmail LIKE CONCAT('%', :email, '%')) AND " +
            "DATE(u.createdAt) = :date"
    )
    Page<ActionHistory> findByActionHistoryWithDate(
            @Param("statuses") List<String> statuses,
            @Param("roles") List<String> roles,
            @Param("date") LocalDate date,
            @Param("company") String company,
            @Param("name") String name,
            @Param("email") String email,
            Pageable pageable);

    @Query("SELECT u FROM ActionHistory u WHERE " +
            "u.action IN (:statuses) AND " +
            "u.userRole IN (:roles) AND " +
            "(u.userName LIKE CONCAT('%', :name, '%') OR u.updateByAdminName LIKE CONCAT('%', :name, '%')) AND " +
            "(u.userEmail LIKE CONCAT('%', :email, '%') OR u.updateByAdminEmail LIKE CONCAT('%', :email, '%')) AND " +
            "(u.userCompany LIKE CONCAT('%', :company, '%'))"
    )
    Page<ActionHistory> findByActionHistoryWithoutDate(
            @Param("statuses") List<String> statuses,
            @Param("roles") List<String> roles,
            @Param("company") String company,
            @Param("name") String name,
            @Param("email") String email,
            Pageable pageable);
}
