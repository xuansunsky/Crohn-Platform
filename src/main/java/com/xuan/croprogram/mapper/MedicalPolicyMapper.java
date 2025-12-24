package com.xuan.croprogram.mapper;


import com.xuan.croprogram.model.MedicalPolicy;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MedicalPolicyMapper {

    // 🔍 查：根据城市和类型，找政策
    @Select("SELECT * FROM medical_policies WHERE city_code = #{cityCode} AND policy_type = #{type} LIMIT 1")
    MedicalPolicy findByCityAndType(String cityCode, String type);

    // ➕ 增：插入一条新政策
    // ➕ 增：【修复版】把漏掉的兄弟们都加上！
    // 注意：dualNote 对应数据库里的 dual_note (假设你数据库字段叫这个)
    @Insert("INSERT INTO medical_policies(" +
            "city_code, city_name, policy_type, is_mente, is_dual, dual_ratio, " +
            "threshold, ratio, cap, dual_note, summary, contributor" + // 🔥 补上了这4个！
            ") VALUES(" +
            "#{cityCode}, #{cityName}, #{policyType}, #{isMente}, #{isDual}, #{dualRatio}, " +
            "#{threshold}, #{ratio}, #{cap}, #{dualNote}, #{summary}, #{contributor}" + // 🔥 对应的值也补上！
            ")")
    void insert(MedicalPolicy policy);

    // 🔧 改：【修复版】更新的时候，也要把这些字段带上，不然以后改了封顶线存不进去
    @Update("UPDATE medical_policies SET " +
            "is_mente = #{isMente}, " +       // 补上
            "is_dual = #{isDual}, " +         // 补上
            "dual_ratio = #{dualRatio}, " +
            "threshold = #{threshold}, " +    // 补上
            "ratio = #{ratio}, " +            // 补上
            "cap = #{cap}, " +                // 补上
            "dual_note = #{dualNote}, " +     // 补上
            "summary = #{summary}, " +
            "contributor = #{contributor}, " + // 更新贡献者名字
            "update_time = NOW() " +
            "WHERE city_code = #{cityCode} AND policy_type = #{policyType}")
    void update(MedicalPolicy policy);
}