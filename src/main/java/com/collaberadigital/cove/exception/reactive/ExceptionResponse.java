package com.collaberadigital.cove.exception.reactive;

public record ExceptionResponse(
        String status,
        String title,
        String statusType,
        String message
) {
}
