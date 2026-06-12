package com.xuan.croprogram.mapper;


import com.xuan.croprogram.model.DietReport;
import com.xuan.croprogram.model.DietFoodComment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface DietReportMapper {

    // 插入病友的评价流水
    @Insert("INSERT INTO diet_food_reports(food_id, user_id, reaction_level, location, content, images_json, is_deleted) " +
            "VALUES(#{foodId}, #{userId}, #{reactionLevel}, #{location}, #{content}, #{imagesJson}, 0)")
    void insert(DietReport report);

    @Select("SELECT * FROM diet_food_reports WHERE id = #{id} AND is_deleted = 0")
    DietReport findById(Long id);

    @Select("SELECT * FROM diet_food_reports WHERE food_id = #{foodId} AND user_id = #{userId} AND is_deleted = 0 ORDER BY id DESC LIMIT 1")
    DietReport findMineByFoodId(@Param("foodId") Long foodId, @Param("userId") Long userId);

    // 根据食物ID查实测，倒序排列
    @Select("""
            SELECT r.*, u.nickname AS user_name, u.avatar AS user_avatar
            FROM diet_food_reports r
            LEFT JOIN account_users u ON u.user_id = r.user_id
            WHERE r.food_id = #{foodId} AND r.is_deleted = 0
            ORDER BY r.id DESC
            """)
    List<DietReport> getReportsByFoodId(Long foodId);

    @Select("""
            SELECT r.*, f.brand_name AS brand, f.food_name AS product,
              COALESCE(NULLIF(r.images_json, ''), f.cover_img) AS cover_img,
              u.nickname AS user_name, u.avatar AS user_avatar
            FROM diet_food_reports r
            LEFT JOIN diet_foods f ON f.id = r.food_id
            LEFT JOIN account_users u ON u.user_id = r.user_id
            WHERE r.user_id = #{userId}
              AND r.is_deleted = 0
              AND NULLIF(TRIM(r.content), '') IS NOT NULL
            ORDER BY r.id DESC
            """)
    List<DietReport> getReportsByUserId(Long userId);

    @Update("""
            UPDATE diet_food_reports
            SET reaction_level = #{reactionLevel},
                location = #{location},
                content = #{content},
                images_json = #{imagesJson}
            WHERE id = #{id} AND user_id = #{userId} AND is_deleted = 0
            """)
    int updateByOwner(DietReport report);

    @Update("UPDATE diet_food_reports SET is_deleted = 1 WHERE id = #{reportId} AND user_id = #{userId} AND is_deleted = 0")
    int softDeleteByOwner(@Param("reportId") Long reportId, @Param("userId") Long userId);

    @Insert("INSERT INTO diet_food_comments(food_id, user_id, content, is_deleted, create_time) VALUES(#{foodId}, #{userId}, #{content}, 0, NOW())")
    void insertFoodComment(DietFoodComment comment);

    @Select("""
            SELECT c.*, u.nickname AS user_name, u.avatar AS user_avatar
            FROM diet_food_comments c
            LEFT JOIN account_users u ON u.user_id = c.user_id
            WHERE c.food_id = #{foodId} AND c.is_deleted = 0
            ORDER BY c.id DESC
            """)
    List<DietFoodComment> getFoodComments(Long foodId);

    @Update("UPDATE diet_food_comments SET is_deleted = 1 WHERE id = #{commentId} AND food_id = #{foodId} AND user_id = #{userId} AND is_deleted = 0")
    int softDeleteFoodCommentByOwner(
            @Param("foodId") Long foodId,
            @Param("commentId") Long commentId,
            @Param("userId") Long userId
    );
}
