package com.collaberadigital.cove.controller.impl;

import com.collaberadigital.cove.controller.AdminApiVersion;
import com.collaberadigital.cove.dto.response.ActionHistoryDto;
import com.collaberadigital.cove.dto.response.UserDetailsDto;
import com.collaberadigital.cove.model.SuccessResponse;
import com.collaberadigital.cove.model.entity.ActionHistory;
import com.collaberadigital.cove.model.entity.UserEntity;
import com.collaberadigital.cove.service.ActionHistoryService;
import com.collaberadigital.cove.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
public class AdminController implements AdminApiVersion {

    private final AdminService adminService;
    private final ActionHistoryService actionHistoryService;


    AdminController(AdminService adminService,ActionHistoryService actionHistoryService){
    this.adminService = adminService;
    this.actionHistoryService = actionHistoryService;
    }

    @PutMapping("/{toEmail}/onboarding-status")
    public Mono<SuccessResponse> updateOnBoardingStatusController(
            @PathVariable String toEmail,
            @RequestParam String status,
            @RequestParam(defaultValue = "") String description,
            @RequestHeader Map<String, Object> headers){

        return  adminService.updateUserOnboardingStatus(headers,toEmail,status,description);
    }

    @PutMapping("/{toEmail}/account-status")
    public Mono<SuccessResponse> updateAccountStatusController(
            @PathVariable String toEmail,
            @RequestParam(defaultValue = "true") Boolean status,
            @RequestParam(defaultValue = "") String description,
            @RequestHeader Map<String, Object> headers){

        return  adminService.updateUserAccountStatus(headers,toEmail,status,description);
    }

    @PutMapping("/{toEmail}/role")
    public Mono<SuccessResponse> updateUserRoleController(
            @PathVariable String toEmail,
            @RequestParam String role,
            @RequestParam(defaultValue = "") String description,
            @RequestHeader Map<String, Object> headers){

        return  adminService.updateUserRole(headers,toEmail,role,description);
    }

    @GetMapping("/user-details/{email}")
    public Mono<UserDetailsDto> getUserDetailsController(@RequestHeader Map<String, Object> headers,@PathVariable String email){
        return adminService.getUserDetails(headers,email);
    }


    @GetMapping("/users")
    public Mono<Page<UserDetailsDto>> getAllUserPaginationController(
            @RequestHeader Map<String, Object> headers,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "userId") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending,
            @RequestParam(defaultValue = "pending_approval") List<String> statuses,
            @RequestParam(defaultValue = "CUSTOMER,ADMIN") List<String>  role,
            @RequestParam(defaultValue = "") String submission_date,
            @RequestParam(defaultValue = "") String user_name,
            @RequestParam(defaultValue = "") String company,
            @RequestParam(defaultValue = "true") Boolean active){


        return adminService.getAllUserPagination(
                headers,
                page,
                size,
                sortBy,
                ascending,
                statuses,
                role,
                user_name,
                submission_date,
                company,
                active);
    }

    @GetMapping("/action-history")
    public Mono<Page<ActionHistoryDto>> listActionHistory(
            @RequestHeader Map<String, Object> headers,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending,
            @RequestParam(defaultValue = "reject,approved") List<String> statuses,
            @RequestParam(defaultValue = "CUSTOMER,ADMIN") List<String>  role,
            @RequestParam(defaultValue = "") String date,
            @RequestParam(defaultValue = "") String company,
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "") String email){

        return actionHistoryService.getActionHistoryPagination(
                headers,
                page,
                size,
                sortBy,
                ascending,
                statuses,
                role,
                date,
                company,
                name,
                email);

    }




}
