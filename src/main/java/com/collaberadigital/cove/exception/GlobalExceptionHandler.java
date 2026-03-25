package com.collaberadigital.cove.exception;

import java.util.HashMap;
import java.util.Map;



import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.collaberadigital.cove.model.ErrorDetail;

import org.springframework.web.bind.support.WebExchangeBindException;



@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler{

	@ExceptionHandler(CoveCustomException.class)
	public ResponseEntity<?> coveExceptionhandler(CoveCustomException ex) {
		ErrorDetail errorDetail = createErrorResponse(ex.getErrorMessage(), ex.getStatusCode());
		errorDetail.setVendorErrorMessage(ex.getVendorError());
		Map<String,Object> errorWrapper = new HashMap<String, Object>();
		
		errorWrapper.put("status", ex.getStatusCode().value());
		errorWrapper.put("message", ex.getErrorMessage());
		errorWrapper.put("error", errorDetail);
		
		return new ResponseEntity<>(errorWrapper, ex.getStatusCode());
}
	
	@ExceptionHandler(WebExchangeBindException.class)
	protected ResponseEntity<Object> handleMethodArgumentNotValid(WebExchangeBindException ex) {
		ErrorDetail errorDetail = createErrorResponse(ex.getMessage(),HttpStatus.BAD_REQUEST);
		errorDetail.setVendorErrorMessage("Please provide valid input");
		Map<String,Object> errorWrapper = new HashMap<String, Object>();
		
		errorWrapper.put("status", HttpStatus.BAD_REQUEST.value());
		errorWrapper.put("message", "Bad Request");
		errorWrapper.put("error", errorDetail);
		
		return new ResponseEntity<>(errorWrapper, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException ex) {
		ErrorDetail errorDetail = createErrorResponse(ex.getMessage(),HttpStatus.BAD_REQUEST);
		errorDetail.setVendorErrorMessage("Please provide valid input");
		Map<String,Object> errorWrapper = new HashMap<String, Object>();
		
		errorWrapper.put("status", HttpStatus.BAD_REQUEST.value());
		errorWrapper.put("message", "Invalid Credentails");
		errorWrapper.put("error", errorDetail);
		
		return new ResponseEntity<>(errorWrapper, HttpStatus.BAD_REQUEST);
    }
	
	@ExceptionHandler(ServerWebInputException.class)
	protected ResponseEntity<Object> handleMethodArgumentNotValid(ServerWebInputException ex) {
		ErrorDetail errorDetail = createErrorResponse(ex.getMessage(),HttpStatus.BAD_REQUEST);
		errorDetail.setVendorErrorMessage("Please provide valid input");
		Map<String,Object> errorWrapper = new HashMap<String, Object>();
		
		errorWrapper.put("status", HttpStatus.BAD_REQUEST.value());
		errorWrapper.put("message", "Bad Request");
		errorWrapper.put("error", errorDetail);
		
		return new ResponseEntity<>(errorWrapper, HttpStatus.BAD_REQUEST);
	}
	
	private ErrorDetail createErrorResponse(String message, HttpStatus httpStatus) {
		ErrorDetail error = new ErrorDetail();
		error.setCode(httpStatus.value());
		if(message.contains("Please provide a valid role"))
			error.setMessage("Please provide a valid role");
		else if(message.contains("Role is required field"))
			error.setMessage("Role is required field");
		else if(message.contains("Request body is missing"))
			error.setMessage("Request body is missing");
		else
		error.setMessage(message);
		return error;
	}
	
}
