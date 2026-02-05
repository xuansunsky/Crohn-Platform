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
    @Insert("INSERT INTO users(phone_number, password, nickname, role_id) " +
            "VALUES(#{phoneNumber}, #{password}, #{nickname}, #{roleId})")
    void insertUser(User user);
    // 1. 拉取所有子民清单（排除掉敏感的密码，只拿关键信息）
    @Select("SELECT id, phone_number as phoneNumber, nickname as nickname, role_id as roleId FROM users")
    List<User> findAllUsers();

    // 2. 修改角色：这就是“封王”或“贬职”的核心动作
    @Update("UPDATE users SET role_id = #{roleId} WHERE id = #{userId}")
    void updateRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    // 👇 2. 新增：这是我们要补的查询逻辑
    // 专门查 role_id，不用把整个 User 对象都查出来，节省内存
    @Select("SELECT role_id FROM users WHERE id = #{userId}")
    Long selectRoleIdByUserId(@Param("userId") Long userId);

}
