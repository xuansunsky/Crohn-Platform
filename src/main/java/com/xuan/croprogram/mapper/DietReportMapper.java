package com.xuan.croprogram.mapper;


import com.xuan.croprogram.model.DietReport;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DietReportMapper {

    // 插入病友的评价流水
    @Insert("INSERT INTO t_diet_report(food_id, user_id, reaction_level, location, content, images_json, is_deleted) " +
            "VALUES(#{foodId}, #{userId}, #{reactionLevel}, #{location}, #{content}, #{imagesJson}, 0)")
    void insert(DietReport report);
    // 新增：根据食物ID查战报，倒序排列
    @Select("SELECT * FROM t_diet_report WHERE food_id = #{foodId} AND is_deleted = 0 ORDER BY id DESC")
    List<DietReport> getReportsByFoodId(Long foodId);
}