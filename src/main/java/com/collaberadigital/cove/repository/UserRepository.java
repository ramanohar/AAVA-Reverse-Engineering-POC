package com.collaberadigital.cove.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.collaberadigital.cove.model.entity.UserEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer>  {

	boolean existsByRegistrationId(String registrationId);
	
	  	Optional<UserEntity> findByEmail(String email);
	@Query("SELECT u FROM UserEntity u WHERE " +
			"u.onboardingStatus IN :statuses AND " +
			"u.role IN :roles AND " +
			"(u.firstname LIKE CONCAT('%', :name, '%') OR u.lastname LIKE CONCAT('%', :name, '%')) AND " +
			"(u.company LIKE CONCAT('%', :company, '%')) AND " +
			"u.isActive = :active AND " +
			"DATE(u.createdAt) = :submissionDate"
	)
	Page<UserEntity> findByOnboardingStatusInAndSubmissionDate(
			@Param("statuses") List<String> statuses,
			@Param("roles") List<String> roles,
			@Param("name") String name,
			@Param("submissionDate") LocalDate submissionDate,
			@Param("company") String company,
			@Param("active") Boolean active,
			Pageable pageable);

	@Query("SELECT u FROM UserEntity u WHERE " +
			"u.onboardingStatus IN :statuses AND " +
			"u.role IN :roles AND " +
			"(u.firstname LIKE CONCAT('%', :name, '%') OR u.lastname LIKE CONCAT('%', :name, '%')) AND " +
			"(u.company LIKE CONCAT('%', :company, '%')) AND " +
			"u.isActive = :active"
	)
	Page<UserEntity> findByOnboardingStatusInAndRoleIn(
			@Param("statuses") List<String> statuses,
			@Param("roles") List<String> roles,
			@Param("name") String name,
			@Param("company") String company,
			@Param("active") Boolean active,
			Pageable pageable);
}


