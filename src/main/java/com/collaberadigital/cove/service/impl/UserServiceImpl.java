
package com.collaberadigital.cove.service.impl;

import static com.collaberadigital.cove.constants.SuccessMessages.USER_CREATION_APPROVED;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.collaberadigital.cove.exception.reactive.CustomException;
import com.collaberadigital.cove.model.*;
import com.collaberadigital.cove.security.JwtRefreshTokenUtil;
import com.collaberadigital.cove.security.JwtAccessTokenUtil;
import com.collaberadigital.cove.service.EmailService;
import com.collaberadigital.cove.utils.PayloadUtil;
import com.collaberadigital.cove.utils.constant.ExceptionStatus;
import com.collaberadigital.cove.utils.constant.ExceptionTitle;
import com.collaberadigital.cove.utils.constant.OnboardingStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.collaberadigital.cove.model.entity.UserEntity;
import com.collaberadigital.cove.exception.UserNotFoundException;
import com.collaberadigital.cove.repository.UserRepository;
import com.collaberadigital.cove.service.UserService;
import com.collaberadigital.cove.utils.PojoMapper;

import jakarta.transaction.Transactional;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class UserServiceImpl implements UserService {


    private UserRepository userRepository;
    private  PasswordEncoder encoder;
    private final JwtAccessTokenUtil jwtTokenUtil;
    private final JwtRefreshTokenUtil jwtRefreshTokenUtil;
    private final EmailService emailService;

    UserServiceImpl(
            JwtAccessTokenUtil jwtTokenUtil ,
            PasswordEncoder encoder,
            UserRepository userRepository,
            JwtRefreshTokenUtil jwtRefreshTokenUtil,
            EmailService emailService){
        this.jwtTokenUtil = jwtTokenUtil;
        this.encoder = encoder;
        this.userRepository = userRepository;
        this.jwtRefreshTokenUtil = jwtRefreshTokenUtil;
        this.emailService = emailService;

    }


    @Override
    public List<AuthUser> getUsers(PageRequest pageRequest) {
        Page<UserEntity> pageAssetEntity= userRepository.findAll(pageRequest);
        return pageAssetEntity
                .stream()
                .map(PojoMapper::toUserRecord)
                .collect(Collectors.toList());

    }


    @Override
    @Transactional
    public void approveUser(Integer userId, String userRole) {
        UserEntity entity = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException());
//        entity.setApproved(true);
        entity.setLastUpdatedAt(LocalDateTime.now());
        entity.setLastUpdatedBy(userRole);
        userRepository.save(entity);

        //send user creation confirmation to registered user on approval
        Email email = new Email();
        email.setAttendee(entity.getEmail());
        email.setDescription(USER_CREATION_APPROVED);
    }

    @Override
    public Mono<SuccessResponse> registerUser(AuthUser user) {

        try {
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                //User Already Exists"
                throw new CustomException("400", ExceptionTitle.NotExistingTitle, ExceptionStatus.NotExisting,"User Creation Failed!");
//                throw new CoveCustomException("User Already Exists", HttpStatus.BAD_REQUEST, "Please use a diffrent url to sign up");
            }
            UserEntity userEntity = PayloadUtil.registrationRequestPayload(user);
            userRepository.save(userEntity);

            emailService.sendEmailPendingApproval(userEntity.getEmail(), userEntity.getFirstname()+ " " +userEntity.getLastname());

            SuccessResponse successResponse = new SuccessResponse();
            successResponse.setMessage("User Registration successful. Please wait for the admin to approve your request!");
            successResponse.setStatus(HttpStatus.CREATED.toString());
            return Mono.just(successResponse);
        } catch (Exception e){
            e.printStackTrace();
            throw new CustomException("400", ExceptionTitle.ErrorTitle, ExceptionStatus.Error,"User Creation Failed! "+e.getMessage());

//            throw new CoveCustomException("User Creation Failed!", HttpStatus.BAD_REQUEST, e.getMessage());

        }
    }

    @Override
    public Mono<Token> loginUser(AuthenticationRequest request, Map<String, String> requestParams) {
//        if(requestParams.size()!=0){
//            throw new CustomException("405", "Please provide a valid Url");
//        }


        Optional<UserEntity> userEntity = userRepository.findByEmail(request.getEmail());
        if(userEntity.isPresent()){
            String userOnboardingStatus = userEntity.get().getOnboardingStatus();



            if(userOnboardingStatus.equalsIgnoreCase(OnboardingStatus.Approved)){

                if(!userEntity.get().getIsActive()){
                    throw new CustomException("403", ExceptionTitle.DeactivatedTitle, ExceptionStatus.Deactivated,"Your account has been deactivated.");

                }

               return userRepository.findByEmail(request.getEmail())
                        .map(userDetails -> {
                            if (encoder.matches(request.getPassword(),userDetails.getPassword())) {

                                jwtTokenUtil.revokeAccessToken(userDetails);
                                String token = jwtTokenUtil.generateToken(request.getEmail());
                                jwtTokenUtil.storeAccessToken(userDetails,token);

                                jwtRefreshTokenUtil.revokeRefreshToken(userDetails);
                                String refreshToken = jwtRefreshTokenUtil.generateRefreshToken(request.getEmail());
                                jwtRefreshTokenUtil.storeRefreshToken(userDetails,refreshToken);


                                return Mono.just(PayloadUtil.loginResponsePayload(userDetails.getEmail(),token,refreshToken));
                            } else {
                                throw new BadCredentialsException("Invalid username or password");
                            }
                        }).orElseThrow();

            }
            else if (userOnboardingStatus.equalsIgnoreCase(OnboardingStatus.Reject)) {

                throw new CustomException("403", ExceptionTitle.RejectTitle, ExceptionStatus.Reject,"Unfortunately, you have not been accepted.");

            }
            else if (userOnboardingStatus.equalsIgnoreCase(OnboardingStatus.PendingApproval)) {
                throw new CustomException("403", ExceptionTitle.PendingApprovalTitle, ExceptionStatus.PendingApproval,"Please wait for an email confirmation of your approval.");
            }else {
                throw new CustomException("403", ExceptionTitle.InvalidStatusTitle, ExceptionStatus.InvalidStatus,"Invalid status");
            }
        }else{
            throw new CustomException("403", ExceptionTitle.NotExistingTitle, ExceptionStatus.NotExisting,"Invalid username or password"); //User is not exist in Database
        }



//        return authRequest.flatMap(login -> this.authenticationManager
//                .authenticate(new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPassword()))
//                .map(this.jwtUtil::createToken)).map(jwt -> {
//        Mono<Authentication> authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword()));
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//        Token token = jwtUtil.generateToken(authentication.getName());
//            HttpHeaders httpHeaders = new HttpHeaders();
//            httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + token.getToken());
//            log.info("login has been inititaed for user {}"+ token.getFirstname());
////            return new ResponseEntity<>(jwt, httpHeaders, HttpStatus.OK);
////        });
//        return Mono.just(token);


    }


}
