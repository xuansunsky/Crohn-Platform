package com.xuan.croprogram.mapper;

import com.xuan.croprogram.model.Policy;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface PolicyMapper {

    // 1. 查详情 (查那个 JSON 字符串出来)
    @Select("SELECT * FROM t_policy WHERE city_name = #{city} AND policy_type = #{type} LIMIT 1")
    Policy selectOnePolicy(String city, String type);

    // 2. 新增 (插入 JSON 字符串)
    @Insert("INSERT INTO t_policy(city_name, policy_type, update_time, contributor, " +
            "mente, dual_channel, deductible, nominal_ratio, hidden_self_pay, " +
            "dual_ratio, dual_note, summary, drugs_json) " +
            "VALUES(#{cityName}, #{policyType}, #{updateTime}, #{contributor}, " +
            "#{mente}, #{dualChannel}, #{deductible}, #{nominalRatio}, #{hiddenSelfPay}, " +
            "#{dualRatio}, #{dualNote}, #{summary}, #{drugsJson})")
    void insert(Policy policy);

    // 3. 修改 (更新 JSON 字符串)
    @Update("UPDATE t_policy SET update_time=#{updateTime}, contributor=#{contributor}, " +
            "mente=#{mente}, dual_channel=#{dualChannel}, deductible=#{deductible}, " +
            "nominal_ratio=#{nominalRatio}, hidden_self_pay=#{hiddenSelfPay}, " +
            "dual_ratio=#{dualRatio}, dual_note=#{dualNote}, summary=#{summary}, " +
            "drugs_json=#{drugsJson} " +
            "WHERE city_name=#{cityName} AND policy_type=#{policyType}")
    void update(Policy policy);

    // 4. 检查是否存在
    @Select("SELECT count(*) FROM t_policy WHERE city_name = #{city} AND policy_type = #{type}")
    int count(String city, String type);
}