package com.collaberadigital.cove.service;

import com.collaberadigital.cove.dto.response.UserDetailsDto;
import com.collaberadigital.cove.model.SuccessResponse;
import com.collaberadigital.cove.model.entity.UserEntity;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface AdminService {
    Mono<SuccessResponse> updateUserOnboardingStatus(Map<String, Object> headers, String toEmail, String toOnBoardingStatus,String description);
    Mono<SuccessResponse> updateUserAccountStatus(Map<String, Object> headers, String toEmail, Boolean toAccountStatus,String description);
    Mono<SuccessResponse> updateUserRole(Map<String, Object> headers, String toEmail, String toRole,String description);
    Mono<UserDetailsDto> getUserDetails(Map<String, Object> headers, String userEmail);
    Mono<Page<UserDetailsDto>> getAllUserPagination(Map<String, Object> headers,
                                                int page,
                                                int size,
                                                String sortBy,
                                                boolean ascending,
                                                List<String> statuses,
                                                List<String>  role,
                                                String username,
                                                String submission_date,
                                                String company,
                                                Boolean active);

}
