package com.example.advice;

import com.example.vo.BussinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class AllExceptionHandler {
    /**
     * 全局异常处理
     * @param e
     * @return  错误信息
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public String handleException(Exception e) {
        return "error";
    }
    public ResponseEntity<String> handleBusinessException(BussinessException e) {


        return ResponseEntity.status(HttpStatus.OK).body("error");

    }

    public static void main(String[] args) {
        System.out.println("hello world");
    }
}
