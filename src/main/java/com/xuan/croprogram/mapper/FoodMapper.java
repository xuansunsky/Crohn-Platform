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

    @Select("SELECT * FROM food WHERE id = #{id}")
    Food findById(Long id);

    @Update("""
            UPDATE food f
            JOIN (
              SELECT
                COUNT(*) AS total_votes,
                COALESCE(SUM(CASE WHEN reaction_level = 1 THEN 1 ELSE 0 END), 0) AS level1_votes,
                COALESCE(SUM(CASE WHEN reaction_level = 2 THEN 1 ELSE 0 END), 0) AS level2_votes,
                COALESCE(SUM(CASE WHEN reaction_level = 3 THEN 1 ELSE 0 END), 0) AS level3_votes,
                COALESCE(SUM(CASE WHEN reaction_level = 4 THEN 1 ELSE 0 END), 0) AS level4_votes,
                COALESCE(SUM(CASE WHEN reaction_level = 5 THEN 1 ELSE 0 END), 0) AS level5_votes,
                COALESCE(SUM(CASE WHEN reaction_level = 6 THEN 1 ELSE 0 END), 0) AS level6_votes
              FROM diet_report
              WHERE food_id = #{foodId} AND is_deleted = 0
            ) s
            SET f.total_votes = s.total_votes,
                f.level1_votes = s.level1_votes,
                f.level2_votes = s.level2_votes,
                f.level3_votes = s.level3_votes,
                f.level4_votes = s.level4_votes,
                f.level5_votes = s.level5_votes,
                f.level6_votes = s.level6_votes,
                f.safe_rate = CASE
                  WHEN s.total_votes = 0 THEN 0
                  ELSE ROUND((s.level1_votes + s.level2_votes) * 100 / s.total_votes)
                END
            WHERE f.id = #{foodId}
            """)
    void refreshStats(Long foodId);

    @Update("UPDATE food SET cover_img = #{coverImg} WHERE id = #{foodId}")
    void updateCover(@Param("foodId") Long foodId, @Param("coverImg") String coverImg);
}
