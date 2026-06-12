package com.xuan.croprogram.mapper;

import com.xuan.croprogram.model.ExperienceComment;
import com.xuan.croprogram.model.ExperiencePost;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ExperiencePostMapper {

    // 1. 查所有帖子 (倒序，最新的在前面，连表带出作者昵称头像)
    @Select("""
            SELECT p.*,
                   u.nickname AS authorName,
                   u.avatar AS authorAvatar,
                   (SELECT COUNT(*) FROM experience_post_likes l WHERE l.post_id = p.id) AS likeCount,
                   (SELECT COUNT(*) FROM experience_post_favorites f WHERE f.post_id = p.id) AS favoriteCount,
                   (SELECT COUNT(*) FROM experience_post_comments c WHERE c.post_id = p.id AND c.is_deleted = 0) AS commentCount,
                   CASE WHEN #{viewerId} IS NULL THEN 0
                        ELSE EXISTS(SELECT 1 FROM experience_post_likes l WHERE l.post_id = p.id AND l.user_id = #{viewerId})
                   END AS liked,
                   CASE WHEN #{viewerId} IS NULL THEN 0
                        ELSE EXISTS(SELECT 1 FROM experience_post_favorites f WHERE f.post_id = p.id AND f.user_id = #{viewerId})
                   END AS favorited
            FROM experience_posts p
            LEFT JOIN account_users u ON p.user_id = u.user_id
            ORDER BY p.created_at DESC
            """)
    List<ExperiencePost> findAll(@Param("viewerId") Long viewerId);

    // 2. 新增帖子
    @Insert("INSERT INTO experience_posts(user_id, title, summary, icon, theme, tags, cover_image, media, created_at) " +
            "VALUES(#{userId}, #{title}, #{summary}, #{icon}, #{theme}, #{tags}, #{coverImage}, #{media}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ExperiencePost post);

    // 3. 删除帖子 (根据ID)
    @Delete("DELETE FROM experience_posts WHERE id = #{id}")
    void deleteById(Long id);

    // 4. 查单个帖子 (为了删除前鉴权，看看是不是本人写的)
    @Select("SELECT * FROM experience_posts WHERE id = #{id}")
    ExperiencePost findById(Long id);

    // 5. 查单个帖子详情（连表带作者昵称头像，用于详情页）
    @Select("""
            SELECT p.*,
                   u.nickname AS authorName,
                   u.avatar AS authorAvatar,
                   (SELECT COUNT(*) FROM experience_post_likes l WHERE l.post_id = p.id) AS likeCount,
                   (SELECT COUNT(*) FROM experience_post_favorites f WHERE f.post_id = p.id) AS favoriteCount,
                   (SELECT COUNT(*) FROM experience_post_comments c WHERE c.post_id = p.id AND c.is_deleted = 0) AS commentCount,
                   CASE WHEN #{viewerId} IS NULL THEN 0
                        ELSE EXISTS(SELECT 1 FROM experience_post_likes l WHERE l.post_id = p.id AND l.user_id = #{viewerId})
                   END AS liked,
                   CASE WHEN #{viewerId} IS NULL THEN 0
                        ELSE EXISTS(SELECT 1 FROM experience_post_favorites f WHERE f.post_id = p.id AND f.user_id = #{viewerId})
                   END AS favorited
            FROM experience_posts p
            LEFT JOIN account_users u ON p.user_id = u.user_id
            WHERE p.id = #{id}
            """)
    ExperiencePost findDetailById(@Param("id") Long id, @Param("viewerId") Long viewerId);

    @Update("UPDATE experience_posts SET " +
            "title = #{title}, " +
            "summary = #{summary}, " +
            "icon = #{icon}, " +
            "theme = #{theme}, " +
            "tags = #{tags} " +
            "WHERE id = #{id}") // 👈 这一句是保命的！只改这一条！
    void update(ExperiencePost post);

    @Insert("INSERT IGNORE INTO experience_post_likes(post_id, user_id, created_at) VALUES(#{postId}, #{userId}, NOW())")
    int insertLike(@Param("postId") Long postId, @Param("userId") Long userId);

    @Delete("DELETE FROM experience_post_likes WHERE post_id = #{postId} AND user_id = #{userId}")
    int deleteLike(@Param("postId") Long postId, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM experience_post_likes WHERE post_id = #{postId} AND user_id = #{userId}")
    int checkLiked(@Param("postId") Long postId, @Param("userId") Long userId);

    @Insert("INSERT IGNORE INTO experience_post_favorites(post_id, user_id, created_at) VALUES(#{postId}, #{userId}, NOW())")
    int insertFavorite(@Param("postId") Long postId, @Param("userId") Long userId);

    @Delete("DELETE FROM experience_post_favorites WHERE post_id = #{postId} AND user_id = #{userId}")
    int deleteFavorite(@Param("postId") Long postId, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM experience_post_favorites WHERE post_id = #{postId} AND user_id = #{userId}")
    int checkFavorited(@Param("postId") Long postId, @Param("userId") Long userId);

    @Insert("INSERT INTO experience_post_comments(post_id, user_id, content, is_deleted, created_at, updated_at) VALUES(#{postId}, #{userId}, #{content}, 0, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertComment(ExperienceComment comment);

    @Select("""
            SELECT c.*, u.nickname AS userName, u.avatar AS userAvatar
            FROM experience_post_comments c
            LEFT JOIN account_users u ON u.user_id = c.user_id
            WHERE c.post_id = #{postId} AND c.is_deleted = 0
            ORDER BY c.id DESC
            """)
    List<ExperienceComment> findCommentsByPostId(Long postId);

    @Select("""
            SELECT c.*, u.nickname AS userName, u.avatar AS userAvatar
            FROM experience_post_comments c
            LEFT JOIN account_users u ON u.user_id = c.user_id
            WHERE c.id = #{commentId} AND c.is_deleted = 0
            """)
    ExperienceComment findCommentById(Long commentId);

    @Update("""
            UPDATE experience_post_comments
            SET is_deleted = 1, updated_at = NOW()
            WHERE id = #{commentId}
              AND post_id = #{postId}
              AND is_deleted = 0
              AND (user_id = #{userId} OR #{isAdmin} = 1)
            """)
    int softDeleteComment(
            @Param("postId") Long postId,
            @Param("commentId") Long commentId,
            @Param("userId") Long userId,
            @Param("isAdmin") Integer isAdmin
    );
}
