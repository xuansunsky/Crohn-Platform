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

    // 同城精选列表（排除自己）
    @Select("SELECT u.user_id as userId, u.nickname, u.avatar, u.city, " +
            "p.radar_tags as radarTags, p.radar_sign as radarSign, COALESCE(p.discovery_enabled, 1) as discoveryEnabled " +
            "FROM users u LEFT JOIN user_health_profile p ON u.user_id = p.user_id " +
            "WHERE u.user_id != #{userId} AND COALESCE(u.is_active, 1) = 1 " +
            "AND COALESCE(p.discovery_enabled, 1) = 1 " +
            "AND (u.city = #{city} OR u.city = #{cityWithSuffix} " +
            "OR u.city LIKE CONCAT('%', #{city}, '%') " +
            "OR u.city LIKE CONCAT('%', #{cityWithSuffix}, '%')) " +
            "ORDER BY COALESCE(u.updated_at, u.created_at) DESC, u.user_id DESC " +
            "LIMIT 5")
    List<User> findByCity(@Param("city") String city, @Param("cityWithSuffix") String cityWithSuffix, @Param("userId") Long userId);

    // 远方朋友列表（排除自己；如果有当前城市，就优先排除同城）
    @Select({
            "<script>",
            "SELECT u.user_id as userId, u.nickname, u.avatar, u.city,",
            "p.radar_tags as radarTags, p.radar_sign as radarSign, COALESCE(p.discovery_enabled, 1) as discoveryEnabled",
            "FROM users u LEFT JOIN user_health_profile p ON u.user_id = p.user_id",
            "WHERE u.user_id != #{userId}",
            "AND COALESCE(u.is_active, 1) = 1",
            "AND COALESCE(p.discovery_enabled, 1) = 1",
            "<if test=\"city != null and city != ''\">",
            "AND (u.city IS NULL OR u.city = '' OR NOT (u.city = #{city} OR u.city = #{cityWithSuffix}",
            "OR u.city LIKE CONCAT('%', #{city}, '%')",
            "OR u.city LIKE CONCAT('%', #{cityWithSuffix}, '%')))",
            "</if>",
            "ORDER BY COALESCE(u.updated_at, u.created_at) DESC, u.user_id DESC",
            "LIMIT 5",
            "</script>"
    })
    List<User> findDistantPicks(@Param("city") String city, @Param("cityWithSuffix") String cityWithSuffix, @Param("userId") Long userId);

    // 搜索用户：精确 ID 优先，昵称始终支持模糊搜索（排除自己）
    @Select({
            "<script>",
            "SELECT user_id as userId, nickname, avatar, city FROM users",
            "WHERE user_id != #{userId}",
            "AND COALESCE(is_active, 1) = 1",
            "AND (",
            "  LOWER(COALESCE(nickname, '')) LIKE CONCAT('%', LOWER(#{keyword}), '%')",
            "  <if test='targetId != null'> OR user_id = #{targetId} </if>",
            ")",
            "ORDER BY",
            "  <choose>",
            "    <when test='targetId != null'>",
            "      CASE",
            "        WHEN user_id = #{targetId} THEN 0",
            "        WHEN nickname = #{keyword} THEN 1",
            "        WHEN LOWER(COALESCE(nickname, '')) LIKE CONCAT(LOWER(#{keyword}), '%') THEN 2",
            "        ELSE 3",
            "      END",
            "    </when>",
            "    <otherwise>",
            "      CASE",
            "        WHEN nickname = #{keyword} THEN 0",
            "        WHEN LOWER(COALESCE(nickname, '')) LIKE CONCAT(LOWER(#{keyword}), '%') THEN 1",
            "        ELSE 2",
            "      END",
            "    </otherwise>",
            "  </choose>,",
            "  user_id DESC",
            "LIMIT 20",
            "</script>"
    })
    List<User> searchUsers(@Param("keyword") String keyword, @Param("targetId") Long targetId, @Param("userId") Long userId);

    // 按 userId 查单个用户（含昵称头像）
    @Select("SELECT user_id as userId, nickname, avatar FROM users WHERE user_id = #{userId}")
    User findByUserId(@Param("userId") Long userId);

}
