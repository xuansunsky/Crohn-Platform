package com.xuan.croprogram.mapper;

import com.xuan.croprogram.model.UserHealthProfile;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserCenterMapper {

    /**
     * 通过 LEFT JOIN 一次性查出：用户基础信息 + 健康档案
     */
    @Select({
            "<script>",
            "SELECT ",
            "  u.user_id, u.nickname, u.avatar,",
            "  p.diagnosed_at, p.health_phase, p.phase_start_date,",
            "  p.diet_strategy, p.bowel_status, p.badges,",
            "  p.radar_tags, p.radar_sign",
            "FROM users u",
            "LEFT JOIN user_health_profile p ON u.user_id = p.user_id",
            "WHERE u.user_id = #{userId}",
            "</script>"
    })
    UserHealthProfile getProfileByUserId(Long userId);

    /**
     * 2. 动态更新状态参数 (核心大招)
     * 前端传哪个字段，MyBatis 就只拼接更新哪个字段！
     */
    @Update({
            "<script>",
            "UPDATE user_health_profile",
            "<set>",
            "  ",
            "  <if test='healthPhase != null and healthPhase != \"\"'> health_phase = #{healthPhase}, phase_start_date = CURRENT_DATE, </if>",
            "  <if test='dietStrategy != null and dietStrategy != \"\"'> diet_strategy = #{dietStrategy}, </if>",
            "  <if test='bowelStatus != null and bowelStatus != \"\"'> bowel_status = #{bowelStatus}, </if>",
            "</set>",
            "WHERE user_id = #{userId}",
            "  ",
            "  AND ( #{healthPhase} IS NOT NULL OR #{dietStrategy} IS NOT NULL OR #{bowelStatus} IS NOT NULL )",
            "</script>"
    })
    void updateDynamicStatus(UserHealthProfile profile);

    @Select("SELECT u.nickname, u.avatar, " +
            "h.health_phase as healthPhase, h.diet_strategy as dietStrategy, " +
            "h.bowel_status as bowelStatus, h.phase_start_date as phaseStartDate " +
            "FROM users u " +
            "LEFT JOIN user_health_profile h ON u.user_id = h.user_id " +
            "WHERE u.user_id = #{userId}")
    UserHealthProfile getCenterInfo(Long userId);
    @Update({
            "<script>",
            "UPDATE users",
            "<set>",
            "  <if test='nickname != null and nickname != \"\"'> nickname = #{nickname}, </if>",
            "  <if test='avatar != null and avatar != \"\"'> avatar = #{avatar}, </if>",
            "</set>",
            "WHERE user_id = #{userId}",
            "  AND ( #{nickname} IS NOT NULL OR #{avatar} IS NOT NULL )", // 额外的安全防线：防止生成无效SQL
            "</script>"
    })
    void updateBasicInfo(UserHealthProfile profile);

    @Update({
            "<script>",
            "INSERT INTO user_health_profile ",
            "(user_id, health_phase, phase_start_date, diet_strategy, bowel_status) ",
            "VALUES ",
            "(#{userId}, #{healthPhase}, #{phaseStartDate}, #{dietStrategy}, #{bowelStatus}) ",
            "ON DUPLICATE KEY UPDATE ",
            "  <if test='healthPhase != null'> health_phase = VALUES(health_phase), </if>",
            "  <if test='phaseStartDate != null'> phase_start_date = VALUES(phase_start_date), </if>",
            "  <if test='dietStrategy != null'> diet_strategy = VALUES(diet_strategy), </if>",
            "  <if test='bowelStatus != null'> bowel_status = VALUES(bowel_status) </if>",
            "</script>"
    })
    void saveOrUpdateHealthProfile(UserHealthProfile profile);
    /**
     * 专门更新徽章字段
     * 无论前端传来的是 ["徽章A"] 还是清空了徽章 ("[]" 或 null)，都直接覆盖覆盖数据库
     */
    @Update({
            "UPDATE user_health_profile",
            "SET badges = #{badges}",
            "WHERE user_id = #{userId}"
    })
    void updateBadges(UserHealthProfile profile);
}

