package com.example.control;

import com.example.service.VerificationService;
import com.example.vo.VerificationRequestVO;
import com.example.vo.VerificationResultVO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/verification-codes")
public class VerificationController {
    private final VerificationService verificationService;

    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/push")
    public ResponseEntity<VerificationResultVO> push(@RequestBody VerificationRequestVO request) {
        VerificationResultVO result = verificationService.pushCode(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
