package com.collaberadigital.cove.security;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.collaberadigital.cove.exception.reactive.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.collaberadigital.cove.model.ErrorDetail;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@Component
public class JwtTokenAuthenticationFilter implements WebFilter {


    private final JwtAccessTokenUtil tokenProvider;


    private final CustomUserDetailsService customUserDetailsService;


    public JwtTokenAuthenticationFilter(JwtAccessTokenUtil tokenProvider, CustomUserDetailsService customUserDetailsService) {
		this.tokenProvider = tokenProvider;
        this.customUserDetailsService = customUserDetailsService;

	}

	@Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = resolveToken(exchange.getRequest());
//        String tokenExtracted = getJWTFromRequest(request);
        try {


            if (StringUtils.hasText(token) && tokenProvider.validateAccessToken(token)) {
//                if (this.tokenProvider.checkBlackList(token)) {
                    String username = tokenProvider.extractUserUniqueValue(token);

                     return customUserDetailsService.findByUsername(username)
                            .flatMap(userDetails -> {
                                // Create the authentication token
                                UsernamePasswordAuthenticationToken authenticationToken =
                                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                                // Set the authentication in the security context
                                return chain.filter(exchange)
                                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authenticationToken));
                            })
                            .onErrorResume(CustomException.class, e -> {
                                // Handle CustomException
                                return this.onError(exchange, e.getMessage(), HttpStatus.UNAUTHORIZED);
                            })
                            .onErrorResume(e -> {
                                // Handle other exceptions
                                return this.onError(exchange, "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
                            });
//                 } else {
//                    return this.onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
//                }
            }

//        	if (StringUtils.hasText(token) && !this.tokenProvider.isTokenExpired(token)) {
//
//            	if(this.tokenProvider.checkBlackList(token)) {
//
//                    String username = tokenProvider.extractUserUniqueValue(token);
//                    Mono<UserDetails> userDetails = customUserDetailsService.findByUsername(username);
//                   UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
//                            userDetails.getAuthorities());
//            		Authentication authentication = this.tokenProvider.getAuthentication(token.substring(7));
//
//                    return chain.filter(exchange)
//                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
//            	}else {
//            		return this.onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);        	}
//
//            }
        }catch (Exception e) {

            e.printStackTrace();
        	return this.onError(exchange, e.getMessage(), HttpStatus.UNAUTHORIZED);
		}

        return chain.filter(exchange);
    }

    private String resolveToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken)) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response=exchange.getResponse();
        response.setStatusCode(httpStatus);
        Map m=new HashMap<>();
        ErrorDetail errorDeatils= new ErrorDetail();
        errorDeatils.setCode(httpStatus.value());
        errorDeatils.setMessage(err);
        errorDeatils.setVendorErrorMessage(err);
        m.put("message", err);
        m.put("code", httpStatus.value());
        m.put("error",errorDeatils);
        ObjectMapper mapper = new ObjectMapper();
        
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer=null;
		try {
			buffer = response.bufferFactory().wrap(mapper.writeValueAsString(m).getBytes(StandardCharsets.UTF_8));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return response.writeWith(Mono.just(buffer));
    }

    private String getJWTFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

}
