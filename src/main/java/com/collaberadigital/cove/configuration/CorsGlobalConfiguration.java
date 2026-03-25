//package com.collaberadigital.cove.configuration;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.Ordered;
//import org.springframework.core.annotation.Order;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.http.server.reactive.ServerHttpResponse;
//import org.springframework.web.server.ServerWebExchange;
//import org.springframework.web.server.WebFilter;
//import org.springframework.web.server.WebFilterChain;
//import reactor.core.publisher.Mono;
//
//@Configuration
//@Order(Ordered.HIGHEST_PRECEDENCE)
//public class CorsGlobalConfiguration implements WebFilter {
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
//
//        ServerHttpRequest request = exchange.getRequest();
//        ServerHttpResponse response = exchange.getResponse();
//
//        response.getHeaders().add("Access-Control-Allow-Origin", "*");
//        response.getHeaders().add("Access-Control-Allow-Methods", "*");
//        response.getHeaders().add("Access-Control-Allow-Headers", "*");
//
//        if (request.getMethod().equals(HttpMethod.OPTIONS)) {
//            response.setStatusCode(HttpStatus.OK);
//            return Mono.empty();
//        } else {
//            return chain.filter(exchange);
//        }
//    }
//}
