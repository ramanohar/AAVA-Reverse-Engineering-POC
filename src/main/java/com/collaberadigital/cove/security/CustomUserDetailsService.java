package com.collaberadigital.cove.security;

import com.collaberadigital.cove.exception.reactive.CustomException;
import com.collaberadigital.cove.repository.CustomerDao;
import com.collaberadigital.cove.utils.constant.ExceptionStatus;
import com.collaberadigital.cove.utils.constant.ExceptionTitle;
import com.collaberadigital.cove.utils.constant.OnboardingStatus;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CustomUserDetailsService implements ReactiveUserDetailsService {


    private CustomerDao userRepository;

    public CustomUserDetailsService(CustomerDao userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByEmailReactive(username)
                .flatMap(userEntity -> {

                    if (!userEntity.getIsActive()) {
                        return Mono.error(new CustomException("403", ExceptionTitle.DeactivatedTitle, ExceptionStatus.Deactivated, "User account is deactivated"));
                    }
                    if(userEntity.getOnboardingStatus().equalsIgnoreCase(OnboardingStatus.Reject)) {
                        return Mono.error(new CustomException("403", ExceptionTitle.RejectTitle, ExceptionStatus.Reject, "Unfortunately, you have not been accepted."));
                    }
                    if(userEntity.getOnboardingStatus().equalsIgnoreCase(OnboardingStatus.PendingApproval)) {
                        return Mono.error(new CustomException("403", ExceptionTitle.PendingApprovalTitle, ExceptionStatus.PendingApproval, "Please wait for an email confirmation of your approval."));
                    }
                    if(!userEntity.getOnboardingStatus().equalsIgnoreCase(OnboardingStatus.Approved)) {
                        return Mono.error(new CustomException("403", ExceptionTitle.InvalidStatusTitle, ExceptionStatus.InvalidStatus, "Invalid status"));
                    }

                    userEntity.setRole("ROLE_"+userEntity.getRole());
                    return Mono.just((UserDetails) new CurrentUser (userEntity));
                })
                .switchIfEmpty(Mono.error(new CustomException("404", ExceptionTitle.NotExistingTitle, ExceptionStatus.NotExisting, "User  not found")));
    }


}
