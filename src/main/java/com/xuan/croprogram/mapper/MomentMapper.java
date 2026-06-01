package com.xuan.croprogram.mapper;

import com.xuan.croprogram.model.Moment;
import com.xuan.croprogram.model.MomentComment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MomentMapper {

    @Select("SELECT m.*, u.nickname, u.avatar FROM moments m LEFT JOIN users u ON m.user_id = u.user_id ORDER BY m.created_at DESC")
    List<Moment> findAll();

    @Select("SELECT * FROM moments WHERE id = #{id}")
    Moment findById(Long id);

    @Insert("INSERT INTO moments(user_id, content, images_json, device, location) VALUES(#{userId}, #{content}, #{imagesJson}, #{device}, #{location})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Moment moment);

    @Delete("DELETE FROM moments WHERE id = #{id}")
    void deleteById(Long id);

    @Insert("INSERT IGNORE INTO moment_likes(moment_id, user_id) VALUES(#{momentId}, #{userId})")
    int insertLike(@Param("momentId") Long momentId, @Param("userId") Long userId);

    @Delete("DELETE FROM moment_likes WHERE moment_id = #{momentId} AND user_id = #{userId}")
    int deleteLike(@Param("momentId") Long momentId, @Param("userId") Long userId);

    @Update("UPDATE moments SET likes_count = likes_count + 1 WHERE id = #{id}")
    void incrementLikes(Long id);

    @Update("UPDATE moments SET likes_count = GREATEST(likes_count - 1, 0) WHERE id = #{id}")
    void decrementLikes(Long id);

    @Select("SELECT COUNT(*) FROM moment_likes WHERE moment_id = #{momentId} AND user_id = #{userId}")
    int checkLiked(@Param("momentId") Long momentId, @Param("userId") Long userId);

    // 评论：新增
    @Insert("INSERT INTO moment_comments(moment_id, user_id, content) VALUES(#{momentId}, #{userId}, #{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertComment(MomentComment comment);

    // 评论：按动态查（带昵称头像，按时间升序）
    @Select("SELECT c.*, u.nickname, u.avatar FROM moment_comments c " +
            "LEFT JOIN users u ON c.user_id = u.user_id " +
            "WHERE c.moment_id = #{momentId} ORDER BY c.created_at ASC")
    List<MomentComment> findCommentsByMoment(Long momentId);
}
