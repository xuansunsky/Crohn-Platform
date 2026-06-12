package com.xuan.croprogram.mapper;

import com.xuan.croprogram.model.Moment;
import com.xuan.croprogram.model.MomentComment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MomentMapper {

    @Select("SELECT m.*, u.nickname, u.avatar FROM social_moments m LEFT JOIN account_users u ON m.user_id = u.user_id ORDER BY m.created_at DESC")
    List<Moment> findAll();

    /**
     * 朋友圈动态流：
     * - 只展示自己、好友的动态
     * - private：只有发布者本人可见
     * - public / comrade：好友关系内可见
     */
    @Select("SELECT m.*, u.nickname, u.avatar FROM social_moments m " +
            "LEFT JOIN account_users u ON m.user_id = u.user_id " +
            "WHERE (m.user_id = #{viewerId} " +
            "   OR EXISTS (SELECT 1 FROM friendships f " +
            "      WHERE f.status = 'ACCEPTED' " +
            "      AND ((f.requester_id = #{viewerId} AND f.addressee_id = m.user_id) " +
            "        OR (f.addressee_id = #{viewerId} AND f.requester_id = m.user_id)))) " +
            "AND (m.user_id = #{viewerId} OR COALESCE(m.visibility, 'public') != 'private') " +
            "ORDER BY m.created_at DESC")
    List<Moment> findVisible(@Param("viewerId") Long viewerId, @Param("viewerVerified") int viewerVerified);

    @Select("SELECT m.*, u.nickname, u.avatar FROM social_moments m " +
            "LEFT JOIN account_users u ON m.user_id = u.user_id " +
            "WHERE m.user_id = #{targetId} " +
            "AND (m.user_id = #{viewerId} " +
            "     OR (#{canSeeFriend} = 1 AND COALESCE(m.visibility, 'public') != 'private') " +
            "     OR (#{canSeeGroup} = 1 AND COALESCE(m.visibility, 'public') = 'public')) " +
            "ORDER BY m.created_at DESC")
    List<Moment> findUserVisible(@Param("viewerId") Long viewerId,
                                 @Param("targetId") Long targetId,
                                 @Param("canSeeFriend") int canSeeFriend,
                                 @Param("canSeeGroup") int canSeeGroup);

    @Select("SELECT COUNT(*) FROM crohn_user_verification v WHERE v.user_id = #{userId} AND v.status = 'APPROVED'")
    int isVerified(@Param("userId") Long userId);

    @Select("SELECT * FROM social_moments WHERE id = #{id}")
    Moment findById(Long id);

    @Insert("INSERT INTO social_moments(user_id, content, images_json, device, location, visibility) VALUES(#{userId}, #{content}, #{imagesJson}, #{device}, #{location}, COALESCE(#{visibility}, 'public'))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Moment moment);

    @Update("UPDATE social_moments SET content = #{content}, images_json = #{imagesJson}, location = #{location}, visibility = COALESCE(#{visibility}, 'public') WHERE id = #{id} AND user_id = #{userId}")
    int updateByOwner(Moment moment);

    @Delete("DELETE FROM social_moments WHERE id = #{id}")
    void deleteById(Long id);

    @Insert("INSERT IGNORE INTO social_moment_likes(moment_id, user_id) VALUES(#{momentId}, #{userId})")
    int insertLike(@Param("momentId") Long momentId, @Param("userId") Long userId);

    @Delete("DELETE FROM social_moment_likes WHERE moment_id = #{momentId} AND user_id = #{userId}")
    int deleteLike(@Param("momentId") Long momentId, @Param("userId") Long userId);

    @Update("UPDATE social_moments SET likes_count = likes_count + 1 WHERE id = #{id}")
    void incrementLikes(Long id);

    @Update("UPDATE social_moments SET likes_count = GREATEST(likes_count - 1, 0) WHERE id = #{id}")
    void decrementLikes(Long id);

    @Select("SELECT COUNT(*) FROM social_moment_likes WHERE moment_id = #{momentId} AND user_id = #{userId}")
    int checkLiked(@Param("momentId") Long momentId, @Param("userId") Long userId);

    // 评论：新增
    @Insert("INSERT INTO social_moment_comments(moment_id, user_id, content) VALUES(#{momentId}, #{userId}, #{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertComment(MomentComment comment);

    // 评论：按动态查（带昵称头像，按时间升序）
    @Select("SELECT c.*, u.nickname, u.avatar FROM social_moment_comments c " +
            "LEFT JOIN account_users u ON c.user_id = u.user_id " +
            "WHERE c.moment_id = #{momentId} ORDER BY c.created_at ASC")
    List<MomentComment> findCommentsByMoment(Long momentId);
}
