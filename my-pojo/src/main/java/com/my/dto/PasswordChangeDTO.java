package com.my.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PasswordChangeDTO implements Serializable {
    //用户id
    private Long id;
    //新密码
    private String newPassword;
    //旧密码
    private String oldPassword;
}
