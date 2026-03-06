package com.xuan.croprogram.mapper;

import com.xuan.croprogram.model.Policy;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface PolicyMapper {

    // 1. 【默认详情】连表查：保证拿到的是用户最新的头像和昵称
    @Select("SELECT p.*, u.nickname, u.avatar " +
            "FROM policy p " +
            "LEFT JOIN users u ON p.user_id = u.id " +
            "WHERE p.city_name = #{city} AND p.policy_type = #{type} " +
            "ORDER BY p.audit_status DESC, p.likes DESC, p.update_time DESC " +
            "LIMIT 1")
    Policy selectBestPolicy(@Param("city") String city, @Param("type") String type);

    // 2. 【抽屉列表】同样连表，修正了你之前的 audit_status 拼写错误
    @Select("SELECT p.*, u.nickname, u.avatar " +
            "FROM policy p " +
            "LEFT JOIN users u ON p.user_id = u.id " +
            "WHERE p.city_name = #{city} AND p.policy_type = #{type} " +
            "ORDER BY p.audit_status DESC, p.likes DESC, p.update_time DESC")
    List<Policy> selectHistoryList(@Param("city") String city, @Param("type") String type);

    // 3. 【切换版本】
    @Select("SELECT p.*, u.nickname, u.avatar " +
            "FROM policy p " +
            "LEFT JOIN users u ON p.user_id = u.id " +
            "WHERE p.id = #{id}")
    Policy selectById(Long id);

    @Update("UPDATE policy SET likes = likes + 1 WHERE id = #{id}")
    void incrementLikes(Long id);

    // 🔥 插入：只存 user_id，不存冗余的 nickname 和 avatar
    @Insert("INSERT INTO policy(city_name, policy_type, user_id, update_time, " +
            "mente, dual_channel, deductible, nominal_ratio, hidden_self_pay, " +
            "dual_ratio, dual_note, summary, drugs_json, evidence_imgs, likes, audit_status) " +
            "VALUES(#{cityName}, #{policyType}, #{userId}, #{updateTime}, " +
            "#{mente}, #{dualChannel}, #{deductible}, #{nominalRatio}, #{hiddenSelfPay}, " +
            "#{dualRatio}, #{dualNote}, #{summary}, #{drugsJson}, #{evidenceImgs}, 0, 0)")
    void insert(Policy policy);

    // 🔥 更新：必须带上 user_id 条件，确保“我的政策只能由我改”
    @Update("UPDATE policy SET update_time=#{updateTime}, " +
            "mente=#{mente}, dual_channel=#{dualChannel}, deductible=#{deductible}, " +
            "nominal_ratio=#{nominalRatio}, hidden_self_pay=#{hiddenSelfPay}, " +
            "dual_ratio=#{dualRatio}, dual_note=#{dualNote}, summary=#{summary}, " +
            "drugs_json=#{drugsJson}, evidence_imgs=#{evidenceImgs} " +
            "WHERE city_name=#{cityName} AND policy_type=#{policyType} AND user_id=#{userId}")
    void update(Policy policy);

    @Select("SELECT count(*) FROM policy WHERE city_name = #{city} AND policy_type = #{type} AND user_id = #{userId}")
    int count(@Param("city") String city, @Param("type") String type, @Param("userId") Long userId);
}