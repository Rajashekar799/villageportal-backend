package com.pegadapalli.portal.controller;

import com.pegadapalli.portal.dto.AdminLoginRequest;
import com.pegadapalli.portal.dto.AdminLoginResponse;
import com.pegadapalli.portal.service.AdminAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AdminLoginRequest request) {
        String token = adminAuthService.login(request.getUsername(), request.getPassword());
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid admin credentials"));
        }
        return ResponseEntity.ok(new AdminLoginResponse(token));
    }
}
