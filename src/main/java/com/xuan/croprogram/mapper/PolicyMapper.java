package com.xuan.croprogram.mapper;

import com.xuan.croprogram.model.Policy;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface PolicyMapper {

    // 1. 【默认详情】查排名第一的那条 (官方核验 > 点赞多 > 时间新)
    // 用于页面刚进来时显示
    @Select("SELECT * FROM t_policy " +
            "WHERE city_name = #{city} AND policy_type = #{type} " +
            "ORDER BY audit_status DESC, likes DESC, update_time DESC " +
            "LIMIT 1")
    Policy selectBestPolicy(String city, String type);

    // 2. 【抽屉列表】查该城市的所有历史版本
    // 逻辑和你定的一样：核验优先 -> 点赞优先 -> 时间优先
    @Select("SELECT id, nickname, update_time, audit_status, likes, " +
            "deductible, nominal_ratio, evidence_imgs, user_id " + // 只查列表需要的字段
            "FROM t_policy " +
            "WHERE city_name = #{city} AND policy_type = #{type} " +
            "ORDER BY audit_status DESC, likes DESC, update_time DESC")
    List<Policy> selectHistoryList(String city, String type);

    // 3. 【切换版本】根据 ID 精准查询某一条
    // 用户在抽屉里点谁，就用这个 ID 查谁
    @Select("SELECT * FROM t_policy WHERE id = #{id}")
    Policy selectById(Long id);

    // 🔥 1.【新增】点赞专用方法
    // 逻辑：找到这个ID，把它的 likes 数字加 1
    // 只有用 ID 才能精准打击，不会误伤同城市的其他版本
    @Update("UPDATE t_policy SET likes = likes + 1 WHERE id = #{id}")
    void incrementLikes(Long id);

    // 🔥 2.【修改】插入方法 (加上 likes 字段，默认给 0)
    // 注意看 VALUES 最后的 0，那是给 likes 的初始值
    @Insert("INSERT INTO t_policy(city_name, policy_type, update_time, nickname, " +
            "mente, dual_channel, deductible, nominal_ratio, hidden_self_pay, " +
            "dual_ratio, dual_note, summary, drugs_json, evidence_imgs, likes, audit_status) " + // 👈 加了 likes 和 audit_status
            "VALUES(#{cityName}, #{policyType}, #{updateTime}, #{nickname}, " +
            "#{mente}, #{dualChannel}, #{deductible}, #{nominalRatio}, #{hiddenSelfPay}, " +
            "#{dualRatio}, #{dualNote}, #{summary}, #{drugsJson}, #{evidenceImgs}, 0, 0)") // 👈 对应最后两个 0 (赞0, 未审0)
    void insert(Policy policy);

    // 3. 修改 (🔥 加上 evidence_imgs)
    @Update("UPDATE t_policy SET update_time=#{updateTime}, nickname=#{nickname}, " +
            "mente=#{mente}, dual_channel=#{dualChannel}, deductible=#{deductible}, " +
            "nominal_ratio=#{nominalRatio}, hidden_self_pay=#{hiddenSelfPay}, " +
            "dual_ratio=#{dualRatio}, dual_note=#{dualNote}, summary=#{summary}, " +
            "drugs_json=#{drugsJson}, evidence_imgs=#{evidenceImgs} " + // 👈 这里加了 update 逻辑
            "WHERE city_name=#{cityName} AND policy_type=#{policyType}")
    void update(Policy policy);
    // 4. 检查是否存在
    @Select("SELECT count(*) FROM t_policy WHERE city_name = #{city} AND policy_type = #{type}")
    int count(String city, String type);
}