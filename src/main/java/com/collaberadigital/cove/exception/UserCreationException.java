package com.collaberadigital.cove.exception;

import org.springframework.http.HttpStatus;

public class UserCreationException extends RuntimeException{
    private String message;
    private HttpStatus status;
    private Throwable cause;

    /**
     * Constructs a new runtime exception with the specified detail
     * message, cause, suppression enabled or disabled, and writable
     * stack trace enabled or disabled.
     *
     * @param message            the detail message.
     * @param cause              the cause.  (A {@code null} value is permitted,
     *                           and indicates that the cause is nonexistent or unknown.)
     * @param enableSuppression  whether or not suppression is enabled
     *                           or disabled
     * @param writableStackTrace whether or not the stack trace should
     *                           be writable
     * @since 1.7
     */
    public UserCreationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String message1, HttpStatus status, Throwable cause1) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.message = message1;
        this.status = status;
        this.cause = cause1;
    }

    /**
     * Constructs a new runtime exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public UserCreationException(String message, String message1) {
        super(message);
        this.message = message1;
    }
}
