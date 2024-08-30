package com.my.service;

import com.my.dto.UserLoginDTO;
import com.my.entity.User;

public interface UserService {
    User wxLogin(UserLoginDTO userLoginDTO);
}
