package com.readingsystem.controller;

import com.readingsystem.common.Result;
import com.readingsystem.dto.LoginDTO;
import com.readingsystem.dto.RegisterDTO;
import com.readingsystem.dto.WxLoginDTO;
import com.readingsystem.service.AuthService;
import com.readingsystem.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok(authService.login(dto));
    }

    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.ok(authService.register(dto));
    }

    @PostMapping("/wx-login")
    public Result<LoginVO> wxLogin(@Valid @RequestBody WxLoginDTO dto) {
        return Result.ok(authService.wxLogin(dto));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.ok();
    }
}
