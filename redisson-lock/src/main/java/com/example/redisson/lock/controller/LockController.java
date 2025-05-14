package com.example.redisson.lock.controller;

import com.example.redisson.lock.service.LockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class LockController {

    private final LockService lockService;

    @GetMapping("/lock")
    public String testLock() {
        try {
            lockService.executeWithLock();
            return "SUCCESS";
        } catch (Exception e) {
            return "FAILED: " + e.getMessage();
        }
    }
}
