package com.mogatshoo.dev.config.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	
    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, HttpServletRequest request) {
    	logger.error("예외 발생 요청 URI: " + request.getRequestURI(), ex);
        logger.error("예외 타입: {}", ex.getClass().getName());
        logger.error("예외 메시지: {}", ex.getMessage());
        return "error/globalError";
    }
}
