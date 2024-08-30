package com.my.mapper;

import com.my.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

@Mapper
public interface UserMapper {
    @Select("select * from user where openid = #{openid}")
    User getByOpenId(String openId);

    Long insert(User user);

    @Select("select * from user where id = #{userId}")
    User getById(Long userId);

    @Select("select count(*) from user where date (create_time) = #{date};")
    Integer getNewUserByDate(LocalDate date);
}
