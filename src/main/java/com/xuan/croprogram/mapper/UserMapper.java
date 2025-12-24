package com.xuan.croprogram.mapper;

import com.xuan.croprogram.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;

@Mapper
public interface UserMapper {

    // 查询用户通过手机号
    @Select("SELECT * FROM users WHERE phone_number = #{phoneNumber}")
    User findByPhoneNumber(String phoneNumber);

    // 插入用户
    @Insert("INSERT INTO users(nick_name,phone_number, password) VALUES(#{nickName},#{phoneNumber}, #{password})")
    void insertUser(User user);
}
