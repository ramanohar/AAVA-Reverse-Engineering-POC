package com.collaberadigital.cove.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(value = JsonInclude.Include.NON_EMPTY,content = JsonInclude.Include.NON_NULL)
public class CheckAccessTokenResponse {
    private Boolean active;
    private String sub;
    private List<String> aud;
    private Long  nbf;
    private String scope;
    private Long  exp;
    private Long  iat;
    private String jti;
    private String client_id;
    private String token_type;
    private List<String> authorities;
}
