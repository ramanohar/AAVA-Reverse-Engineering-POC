package com.collaberadigital.cove.utils;

import com.collaberadigital.cove.model.AuthUser;
import com.collaberadigital.cove.model.Token;
import com.collaberadigital.cove.model.entity.ActionHistory;
import com.collaberadigital.cove.model.entity.UserEntity;
import com.collaberadigital.cove.repository.UserRepository;
import com.collaberadigital.cove.utils.constant.OnboardingStatus;
import com.collaberadigital.cove.utils.constant.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public  class PayloadUtil {

    private static PasswordEncoder encoder;
    private static UserRepository userRepository;


    @Autowired
    PayloadUtil(PasswordEncoder encoder,UserRepository userRepository){
        this.encoder = encoder;
        this.userRepository = userRepository;

    }

    public static ActionHistory actionHistoryRequestPayload(
            String userName,
            String userEmail,
            String userRole,
            String userCompany,
            String updateByAdminName,
            String updateByAdminEmail,
            String registrationId,
            String action,
            String adminComments,
            String actionType
    ){
        return ActionHistory.builder()
                .userName(userName)
                .userEmail(userEmail)
                .userRole(userRole)
                .userCompany(userCompany)
                .updateByAdminName(updateByAdminName)
                .updateByAdminEmail(updateByAdminEmail)
                .registrationId(registrationId)
                .action(action)
                .adminComments(adminComments)
                .actionType(actionType)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static UserEntity registrationRequestPayload(AuthUser request){

        return UserEntity.builder()
                .company(request.getCompany())
                .country(request.getCountry())
                .email(request.getEmail())
                .createdAt(LocalDateTime.now())
                .onboardingStatus(OnboardingStatus.PendingApproval)
                .lastUpdatedAt(LocalDateTime.now())
                .lastUpdatedBy("")
                .role(UserRole.Customer)
                .designation(request.getDesignation())
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .password(encoder.encode(request.getPassword()))
                .isActive(false)
                .registrationId(GenerateUtil.generateUniqueRegistrationId())
                .clientId("0012w00001rgIRSAA2")
                .clientName("G - XCHANGE INC")
                .clientOneView("G-Xchange Inc.")
                .build();

    }

    public static Token loginResponsePayload(String email,String token,String refreshToken){
      UserEntity userEntity =  userRepository.findByEmail(email).orElseThrow();
      return Token.builder()
              .accessToken(token)
              .refreshToken(refreshToken)
              .tokenType("Bearer")
              .firstname(userEntity.getFirstname())
              .lastname(userEntity.getLastname())
              .email(userEntity.getEmail())
              .account(userEntity.getCompany())
              .role(userEntity.getRole())
              .clientId(userEntity.getClientId())
              .clientName(userEntity.getClientName())
              .clientOneView(userEntity.getClientOneView())
              .build();

    }

    public static String extractToken(Map<String, Object> requestHeaders){
        String authHeader = null;

        if (requestHeaders.containsKey("Authorization")) {
            authHeader = String.valueOf(requestHeaders.get("Authorization"));
        } else if (requestHeaders.containsKey("authorization")) {
            authHeader = String.valueOf(requestHeaders.get("authorization"));
        }

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
//        else {
//            throw new CustomException("451","No valid Authorization header found");
//        }
        return "";
    }

}
