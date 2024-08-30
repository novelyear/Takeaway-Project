package com.my.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.my.constant.MessageConstant;
import com.my.dto.UserLoginDTO;
import com.my.entity.User;
import com.my.exception.LoginFailedException;
import com.my.mapper.UserMapper;
import com.my.properties.WeChatProperties;
import com.my.service.UserService;
import com.my.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    public static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";
    @Autowired
    private WeChatProperties properties;
    @Autowired
    private UserMapper userMapper;

    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        //发送http请求获取openid
        String openId = getOpenId(userLoginDTO.getCode());

        //判断openid合法
        if(openId == null || openId.isEmpty()) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //用户是否为新用户
        User user = userMapper.getByOpenId(openId);
        //新用户需要插入数据库注册
        if(user == null) {
            user = User.builder()
                    .openid(openId)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
        //返回用户对象
        return user;
    }
    private String getOpenId(String code) {
        Map<String, String> map = new HashMap<>();
        map.put("appid", properties.getAppid());
        map.put("secret", properties.getSecret());
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");

        String json = HttpClientUtil.doGet(WX_LOGIN_URL, map);

        JSONObject jsonObject = JSON.parseObject(json);
        return jsonObject.getString("openid");
    }
}
