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
    @Select("SELECT user_id, phone_number as phoneNumber, nickname as nickname, role_id as roleId FROM users")
    List<User> findAllUsers();

    // 2. 修改角色：这就是“封王”或“贬职”的核心动作
    @Update("UPDATE users SET role_id = #{roleId} WHERE user_id = #{userId}")
    void updateRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    // 👇 2. 新增：这是我们要补的查询逻辑
    // 专门查 role_id，不用把整个 User 对象都查出来，节省内存
    @Select("SELECT role_id FROM users WHERE user_id = #{userId}")
    Long selectRoleIdByUserId(@Param("userId") Long userId);

    // 同城用户列表（排除自己）
    @Select("SELECT user_id as userId, nickname, avatar, city FROM users " +
            "WHERE user_id != #{userId} AND is_active = 1 " +
            "AND (city = #{city} OR city = #{cityWithSuffix} OR city LIKE CONCAT('%', #{city}, '市%')) " +
            "LIMIT 20")
    List<User> findByCity(@Param("city") String city, @Param("cityWithSuffix") String cityWithSuffix, @Param("userId") Long userId);

    // 按昵称模糊搜索（排除自己）
    @Select("SELECT user_id as userId, nickname, avatar, city FROM users " +
            "WHERE user_id != #{userId} AND is_active = 1 " +
            "AND nickname LIKE CONCAT('%', #{keyword}, '%') " +
            "LIMIT 20")
    List<User> searchByNickname(@Param("keyword") String keyword, @Param("userId") Long userId);

    // 按用户ID精确查（排除自己）
    @Select("SELECT user_id as userId, nickname, avatar, city FROM users " +
            "WHERE user_id = #{targetId} AND user_id != #{userId} AND is_active = 1")
    List<User> searchById(@Param("targetId") Long targetId, @Param("userId") Long userId);

    // 按 userId 查单个用户（含昵称头像）
    @Select("SELECT user_id as userId, nickname, avatar FROM users WHERE user_id = #{userId}")
    User findByUserId(@Param("userId") Long userId);

}
