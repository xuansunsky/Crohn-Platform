package com.xuan.croprogram.mapper;


import com.xuan.croprogram.model.Food;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FoodMapper {

    // 查：根据品牌和名字找食物（看看以前有没有人建过）
    @Select("SELECT * FROM t_food WHERE brand_name = #{brandName} AND food_name = #{foodName}")
    Food selectByBrandAndName(@Param("brandName") String brandName, @Param("foodName") String foodName);

    // 增：如果没有，就新建一个。并且把自增的 ID 塞回对象里！
    @Insert("INSERT INTO t_food(brand_name, food_name, total_votes, safe_rate, " +
            "level1_votes, level2_votes, level3_votes, level4_votes, level5_votes, level6_votes) " +
            "VALUES(#{brandName}, #{foodName}, 0, 0, 0, 0, 0, 0, 0, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Food food);
    // 在 FoodMapper 接口里加上这句：
    @Select("SELECT * FROM t_food ORDER BY total_votes DESC")
    List<Food> selectAllFoods();
    // 改：每次有人发布战报，直接全量更新这个食物的票数和安全率
    @Update("UPDATE t_food SET total_votes = #{totalVotes}, safe_rate = #{safeRate}, " +
            "level1_votes = #{level1Votes}, level2_votes = #{level2Votes}, " +
            "level3_votes = #{level3Votes}, level4_votes = #{level4Votes}, " +
            "level5_votes = #{level5Votes}, level6_votes = #{level6Votes} " +
            "WHERE id = #{id}")
    void updateStats(Food food);
}