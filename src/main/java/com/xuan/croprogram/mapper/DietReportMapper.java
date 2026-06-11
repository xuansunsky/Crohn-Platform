package com.xuan.croprogram.mapper;


import com.xuan.croprogram.model.DietReport;
import com.xuan.croprogram.model.DietReportComment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface DietReportMapper {

    // 插入病友的评价流水
    @Insert("INSERT INTO diet_report(food_id, user_id, reaction_level, location, content, images_json, is_deleted) " +
            "VALUES(#{foodId}, #{userId}, #{reactionLevel}, #{location}, #{content}, #{imagesJson}, 0)")
    void insert(DietReport report);

    @Select("SELECT * FROM diet_report WHERE id = #{id} AND is_deleted = 0")
    DietReport findById(Long id);

    // 新增：根据食物ID查战报，倒序排列
    @Select("""
            SELECT r.*, u.nickname AS user_name, u.avatar AS user_avatar,
              (SELECT COUNT(*) FROM diet_report_comments c WHERE c.report_id = r.id AND c.is_deleted = 0) AS comment_count
            FROM diet_report r
            LEFT JOIN users u ON u.user_id = r.user_id
            WHERE r.food_id = #{foodId} AND r.is_deleted = 0
            ORDER BY r.id DESC
            """)
    List<DietReport> getReportsByFoodId(Long foodId);

    @Select("""
            SELECT r.*, f.brand_name AS brand, f.food_name AS product,
              COALESCE(NULLIF(r.images_json, ''), f.cover_img) AS cover_img,
              u.nickname AS user_name, u.avatar AS user_avatar,
              (SELECT COUNT(*) FROM diet_report_comments c WHERE c.report_id = r.id AND c.is_deleted = 0) AS comment_count
            FROM diet_report r
            LEFT JOIN food f ON f.id = r.food_id
            LEFT JOIN users u ON u.user_id = r.user_id
            WHERE r.user_id = #{userId} AND r.is_deleted = 0
            ORDER BY r.id DESC
            """)
    List<DietReport> getReportsByUserId(Long userId);

    @Update("""
            UPDATE diet_report
            SET reaction_level = #{reactionLevel},
                location = #{location},
                content = #{content},
                images_json = #{imagesJson}
            WHERE id = #{id} AND user_id = #{userId} AND is_deleted = 0
            """)
    int updateByOwner(DietReport report);

    @Update("UPDATE diet_report SET is_deleted = 1 WHERE id = #{reportId} AND user_id = #{userId} AND is_deleted = 0")
    int softDeleteByOwner(@Param("reportId") Long reportId, @Param("userId") Long userId);

    @Insert("INSERT INTO diet_report_comments(report_id, user_id, content, is_deleted, create_time) VALUES(#{reportId}, #{userId}, #{content}, 0, NOW())")
    void insertComment(DietReportComment comment);

    @Select("""
            SELECT c.*, u.nickname AS user_name, u.avatar AS user_avatar
            FROM diet_report_comments c
            LEFT JOIN users u ON u.user_id = c.user_id
            WHERE c.report_id = #{reportId} AND c.is_deleted = 0
            ORDER BY c.id ASC
            """)
    List<DietReportComment> getCommentsByReportId(Long reportId);
}
