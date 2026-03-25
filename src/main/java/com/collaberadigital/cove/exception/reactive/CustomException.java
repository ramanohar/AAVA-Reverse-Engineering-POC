package com.collaberadigital.cove.exception.reactive;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomException extends RuntimeException{

    private String status;
    private String title;
    private String statusType;
    private String message;
}
