package com.xuan.croprogram.mapper;


import com.xuan.croprogram.model.Food;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FoodMapper {

    // 查：根据品牌和名字找食物（看看以前有没有人建过）
    @Select("SELECT * FROM food WHERE brand_name = #{brandName} AND food_name = #{foodName}")
    Food selectByBrandAndName(@Param("brandName") String brandName, @Param("foodName") String foodName);

    @Select("SELECT * FROM food ORDER BY total_votes DESC")
    List<Food> selectAllFoods();

    // 🚨 增：新建时，把封面图 cover_img 一起塞进数据库！
    @Insert("INSERT INTO food(brand_name, food_name, cover_img, total_votes, safe_rate, " +
            "level1_votes, level2_votes, level3_votes, level4_votes, level5_votes, level6_votes) " +
            "VALUES(#{brandName}, #{foodName}, #{coverImg}, 0, 0, 0, 0, 0, 0, 0, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Food food);

    // 🚨 改：每次有人发布战报更新数据时，顺便把 cover_img 也 Update 一下
    // （防止原本有图片的，更新完之后图片字段变成 null 了）
    @Update("UPDATE food SET cover_img = #{coverImg}, total_votes = #{totalVotes}, safe_rate = #{safeRate}, " +
            "level1_votes = #{level1Votes}, level2_votes = #{level2Votes}, " +
            "level3_votes = #{level3Votes}, level4_votes = #{level4Votes}, " +
            "level5_votes = #{level5Votes}, level6_votes = #{level6Votes} " +
            "WHERE id = #{id}")
    void updateStats(Food food);
}