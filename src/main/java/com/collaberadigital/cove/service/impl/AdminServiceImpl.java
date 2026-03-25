package com.collaberadigital.cove.service.impl;

import com.collaberadigital.cove.dto.response.UserDetailsDto;
import com.collaberadigital.cove.exception.reactive.CustomException;
import com.collaberadigital.cove.model.SuccessResponse;
import com.collaberadigital.cove.model.entity.UserEntity;
import com.collaberadigital.cove.repository.UserRepository;
import com.collaberadigital.cove.security.JwtAccessTokenUtil;
import com.collaberadigital.cove.service.ActionHistoryService;
import com.collaberadigital.cove.service.AdminService;
import com.collaberadigital.cove.service.EmailService;
import com.collaberadigital.cove.utils.JsonUtility;
import com.collaberadigital.cove.utils.PayloadUtil;
import com.collaberadigital.cove.utils.constant.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final ActionHistoryService actionHistoryService;
    private final JwtAccessTokenUtil jwtUtil;


    AdminServiceImpl(UserRepository userRepository,
                     EmailService emailService,
                     ActionHistoryService actionHistoryService,
                     JwtAccessTokenUtil jwtUtil){
    this.userRepository = userRepository;
    this.emailService = emailService;
    this.actionHistoryService = actionHistoryService;
    this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<SuccessResponse> updateUserOnboardingStatus(Map<String, Object> headers, String toEmail, String toOnBoardingStatus,String description) {
        String token = PayloadUtil.extractToken(headers);
        String adminEmailWillUpdate =jwtUtil.extractUserUniqueValue(token);

        Optional<SuccessResponse> successResponse = userRepository.findByEmail(toEmail)
                .map(userToUpdate -> {
                    if(toOnBoardingStatus.equalsIgnoreCase(OnboardingStatus.Approved) ){
                        userToUpdate.setIsActive(true);
                        emailService.sendEmailApproved(toEmail,userToUpdate.getFirstname() +" "+userToUpdate.getLastname());

                    }
                    if(toOnBoardingStatus.equalsIgnoreCase(OnboardingStatus.Reject) ){
                        userToUpdate.setIsActive(false);
                        emailService.sendEmailReject(toEmail,description,userToUpdate.getFirstname() +" "+userToUpdate.getLastname());
                    }
                    if(toOnBoardingStatus.equalsIgnoreCase(OnboardingStatus.PendingApproval) ){
                        userToUpdate.setIsActive(false);
                    }
                    userToUpdate.setOnboardingStatus(toOnBoardingStatus.toLowerCase());
                    actionHistoryService.saveActionHistory(adminEmailWillUpdate,toEmail,userToUpdate.getOnboardingStatus(),description,ActionType.UpdateOnBoardingStatus);

                    userRepository.save(userToUpdate);
                    return new SuccessResponse("User onboarding status updated successfully","200");
                });

        return successResponse.map(Mono::just
        ).orElseGet(() ->
                Mono.error(new CustomException("400", ExceptionTitle.NotExistingTitle, ExceptionStatus.NotExisting, "User  " + toEmail + " does not exist")));



    }

    @Override
    public Mono<SuccessResponse> updateUserAccountStatus(Map<String, Object> headers, String toEmail, Boolean toAccountStatus,String description) {
        String token = PayloadUtil.extractToken(headers);
        String adminEmailWillUpdate =jwtUtil.extractUserUniqueValue(token);

        Optional<SuccessResponse> successResponse = userRepository.findByEmail(toEmail)
                .map(userToUpdate -> {
                    userToUpdate.setIsActive(toAccountStatus);
                   UserEntity userEntity = userRepository.save(userToUpdate);

                   String userActiveStatus = "";
                   if(userEntity.getIsActive()){
                       userActiveStatus = AccountStatus.Activated;
                   }
                    if(!userEntity.getIsActive()){
                        userActiveStatus = AccountStatus.Deactivated;
                    }


                    actionHistoryService.saveActionHistory(adminEmailWillUpdate,toEmail,userActiveStatus,description,ActionType.UpdateUserAccountStatus);

                    return new SuccessResponse("User account status updated successfully","200");
                });

        return successResponse.map(Mono::just
        ).orElseGet(() ->
                Mono.error(new CustomException("400", ExceptionTitle.NotExistingTitle, ExceptionStatus.NotExisting, "User  " + toEmail + " does not exist")));
    }

    @Override
    public Mono<SuccessResponse> updateUserRole(Map<String, Object> headers, String toEmail, String toRole,String description) {
        String token = PayloadUtil.extractToken(headers);
        String adminEmailWillUpdate =jwtUtil.extractUserUniqueValue(token);

        Optional<SuccessResponse> successResponse = userRepository.findByEmail(toEmail)
                .map(userToUpdate -> {
                    userToUpdate.setRole(toRole.toUpperCase());
                    userRepository.save(userToUpdate);
                    actionHistoryService.saveActionHistory(adminEmailWillUpdate,toEmail,toRole.toUpperCase(),description,ActionType.UpdateUserRoleStatus);

                    return new SuccessResponse("User role updated successfully","200");
                });

        return successResponse.map(Mono::just
        ).orElseGet(() ->
                Mono.error(new CustomException("400", ExceptionTitle.NotExistingTitle, ExceptionStatus.NotExisting, "User  " + toEmail + " does not exist")));
    }

    @Override
    public Mono<UserDetailsDto> getUserDetails(Map<String, Object> headers, String userEmail) {
//        String token = PayloadUtil.extractToken(headers);
//        String adminEmailWillUpdate =jwtUtil.extractUserUniqueValue(token);

        Optional<UserDetailsDto> successResponse = userRepository.findByEmail(userEmail)
                .map(userDetails -> {
                    System.out.println("User details12: "+userDetails);
                  String userDetailsJson = JsonUtility.toJson(userDetails);
                   return JsonUtility.toObject(userDetailsJson,UserDetailsDto.class);

                });

        return successResponse.map(Mono::just
        ).orElseGet(() ->
                Mono.error(new CustomException("400", ExceptionTitle.NotExistingTitle, ExceptionStatus.NotExisting, "User  " + userEmail + " does not exist")));
    }

    @Override
    public Mono<Page<UserDetailsDto>> getAllUserPagination(Map<String, Object> headers, int page, int size, String sortBy, boolean ascending, List<String> statuses, List<String> role, String username, String submission_date,String company,Boolean active) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Mono<Page<UserEntity>> userEntityPagination;

        if (submission_date == null || submission_date.isEmpty()) {
            userEntityPagination = Mono.just(userRepository.findByOnboardingStatusInAndRoleIn(statuses, role, username,company,active, pageable));
        } else {
            LocalDate localDate = LocalDate.parse(submission_date);
            userEntityPagination  = Mono.just(userRepository.findByOnboardingStatusInAndSubmissionDate(statuses, role, username, localDate,company,active, pageable));
        }

        return userEntityPagination.map(paginationData -> {
          List<UserDetailsDto> userDetailsDtoList =  paginationData
                    .getContent()
                    .stream()
                    .map(contentData -> {
                        String jsonData = JsonUtility.toJson(contentData);
                       return JsonUtility.toObject(jsonData,UserDetailsDto.class);

                    }).collect(Collectors.toList());
            return new PageImpl<>(userDetailsDtoList, paginationData.getPageable(), paginationData.getTotalElements());

        });


    }
}
