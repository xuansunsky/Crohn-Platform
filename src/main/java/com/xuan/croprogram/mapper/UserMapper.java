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
    @Insert("INSERT INTO users(nick_name,phone_number, password) VALUES(#{nickName},#{phoneNumber}, #{password})")
    void insertUser(User user);
    // 🔍 查名册：关联查询，顺便把角色名也带出来（虽然前端可以用 ID 判断，但带上名字更稳）
    @Select("SELECT u.*, r.role_name as roleName FROM users u " +
            "LEFT JOIN roles r ON u.role_id = r.id")
    List<User> findAllUsers();

    // 🛠️ 敕封/贬职：只动 role_id 这一行
    @Update("UPDATE users SET role_id = #{roleId} WHERE id = #{userId}")
    void updateRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
