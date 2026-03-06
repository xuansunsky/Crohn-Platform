package com.xuan.croprogram.mapper;

import com.xuan.croprogram.model.UserHealthProfile;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserCenterMapper {

    @Select("SELECT u.nickname, u.avatar, " +
            "h.health_phase as healthPhase, h.diet_strategy as dietStrategy, " +
            "h.bowel_status as bowelStatus, h.phase_start_date as phaseStartDate " +
            "FROM users u " +
            "LEFT JOIN user_health_profile h ON u.id = h.user_id " +
            "WHERE u.id = #{userId}")
    UserHealthProfile getCenterInfo(Long userId);
    @Update({
            "<script>",
            "UPDATE users",
            "<set>",
            "  <if test='nickname != null and nickname != \"\"'> nickname = #{nickname}, </if>",
            "  <if test='avatar != null and avatar != \"\"'> avatar = #{avatar}, </if>",
            "</set>",
            "WHERE id = #{userId}",
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
}

