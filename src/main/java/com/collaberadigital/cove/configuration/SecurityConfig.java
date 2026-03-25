package com.collaberadigital.cove.configuration;

import com.collaberadigital.cove.security.CustomUserDetailsService;
import com.collaberadigital.cove.security.JwtTokenAuthenticationFilter;
import com.collaberadigital.cove.security.JwtAccessTokenUtil;
import com.collaberadigital.cove.utils.constant.UserRole;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
	private final CustomUserDetailsService customUserDetailsService;
	SecurityConfig(CustomUserDetailsService customUserDetailsService){
		this.customUserDetailsService = customUserDetailsService;
	}




	@Bean
	SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http,
	                                            JwtAccessTokenUtil tokenProvider,
	                                            ReactiveAuthenticationManager reactiveAuthenticationManager) {

	    return http
	        .csrf(ServerHttpSecurity.CsrfSpec::disable)
	        .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)

				.formLogin(ServerHttpSecurity.FormLoginSpec::disable)

	        .authenticationManager(reactiveAuthenticationManager)
	        .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
	        .authorizeExchange(it -> it
					.pathMatchers(
							"/api/v1/admin/*/onboarding-status",
							"/api/v1/admin/*/account-status",
							"/api/v1/admin/*/role",
							"/api/v1/admin/user-details/**",
							"/api/v1/admin/users/**",
							"/api/v1/admin/action-history/**"
					).hasAnyRole(UserRole.Admin)
	            .anyExchange()
					.permitAll()
	        )
	        .addFilterAt(new JwtTokenAuthenticationFilter(tokenProvider,customUserDetailsService), SecurityWebFiltersOrder.HTTP_BASIC)
	        .build();
	}

//	@Bean
//	public ReactiveUserDetailsService userDetailsService(UserRepository userRepository) {
//		return new CustomUserDetailsService(userRepository);
//	}

////    @Bean
//    public ReactiveUserDetailsService userDetailsService(CustomerDao users) {
//
//
//		return username -> users.findByEmailReactive(username)
//                .map(u -> {
//					System.out.println("Test roles: "+ u.getRole());
//					return User
//							.withUsername(u.getEmail())
//							.password(u.getPassword())
////                        .authorities(u.getRole())
////								.authorities("ROLE_" + "CUSTOMER")
////						.authorities( u.getRole())
//							.roles(u.getRole())
//							.build();
//						}
//                );
//    }

	@Bean
	public ReactiveAuthenticationManager reactiveAuthenticationManager(
			@Qualifier("customUserDetailsService") ReactiveUserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder) {
		UserDetailsRepositoryReactiveAuthenticationManager authenticationManager =
				new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
		authenticationManager.setPasswordEncoder(passwordEncoder);
		return authenticationManager;
	}
//	@Bean
//	public ReactiveAuthenticationManager reactiveAuthenticationManager(ReactiveUserDetailsService userDetailsService,
//																	   PasswordEncoder passwordEncoder) {
//		UserDetailsRepositoryReactiveAuthenticationManager authenticationManager =
//				new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
//		authenticationManager.setPasswordEncoder(passwordEncoder);
//		return authenticationManager;
//	}
}
