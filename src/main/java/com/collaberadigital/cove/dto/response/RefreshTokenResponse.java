package com.collaberadigital.cove.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(value = JsonInclude.Include.NON_EMPTY,content = JsonInclude.Include.NON_NULL)
public class RefreshTokenResponse {
    private Boolean active;
    private String refreshToken;
    private String accessToken;
    private String tokenType;

}
