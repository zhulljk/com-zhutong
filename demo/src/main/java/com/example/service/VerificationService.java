package com.example.service;

import com.example.vo.VerificationRequestVO;
import com.example.vo.VerificationResultVO;

public interface VerificationService {
    VerificationResultVO pushCode(VerificationRequestVO request);
}
