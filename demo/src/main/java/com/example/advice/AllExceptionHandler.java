package com.example.advice;

import com.example.vo.BussinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ControllerAdvice
public class AllExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(AllExceptionHandler.class);

    /**
     * 全局异常处理
     * @param e
     * @return  错误信息
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResponseEntity<String> handleException(Exception e) {
        log.error("Exception occurred", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error: " + e.getMessage());
    }

    @ExceptionHandler(BussinessException.class)
    public ResponseEntity<String> handleBusinessException(BussinessException e) {
        return ResponseEntity.status(HttpStatus.OK).body("error");
    }
}
