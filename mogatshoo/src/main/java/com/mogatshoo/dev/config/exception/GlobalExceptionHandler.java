package com.mogatshoo.dev.config.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	
    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex) {
    	logger.error("예외 발생: " + ex);
        return "error/globalError";
    }
    
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<String> handleNoResourceFound(NoResourceFoundException e) {
        // Chrome 개발자 도구 관련 요청은 조용히 처리
        if (e.getMessage().contains(".well-known") || 
            e.getMessage().contains("com.chrome.devtools")) {
            return ResponseEntity.notFound().build();
        }
        
        // 다른 리소스 에러는 기존대로 처리
        return ResponseEntity.notFound().build();
    }
}
