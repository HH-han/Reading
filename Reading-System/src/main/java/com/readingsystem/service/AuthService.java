package com.readingsystem.service;

import com.readingsystem.dto.LoginDTO;
import com.readingsystem.dto.RegisterDTO;
import com.readingsystem.dto.WxLoginDTO;
import com.readingsystem.vo.LoginVO;

public interface AuthService {

    LoginVO login(LoginDTO dto);

    LoginVO wxLogin(WxLoginDTO dto);

    LoginVO register(RegisterDTO dto);

    void logout();
}
