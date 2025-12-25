package com.xuan.croprogram.mapper;

import com.xuan.croprogram.model.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    // 查询用户通过手机号
    @Select("SELECT * FROM users WHERE phone_number = #{phoneNumber}")
    User findByPhoneNumber(String phoneNumber);

    // 插入用户
    @Insert("INSERT INTO users(phone_number, password, nick_name, role_id) " +
            "VALUES(#{phoneNumber}, #{password}, #{nickName}, #{roleId})")
    void insertUser(User user);
    // 1. 拉取所有子民清单（排除掉敏感的密码，只拿关键信息）
    @Select("SELECT id, phone_number as phoneNumber, nick_name as nickName, role_id as roleId FROM users")
    List<User> findAllUsers();

    // 2. 修改角色：这就是“封王”或“贬职”的核心动作
    @Update("UPDATE users SET role_id = #{roleId} WHERE id = #{userId}")
    void updateRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
