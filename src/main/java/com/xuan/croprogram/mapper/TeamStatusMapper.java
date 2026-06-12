package com.xuan.croprogram.mapper;

import com.xuan.croprogram.model.StatusView;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface TeamStatusMapper {

    // ===== 状态本体 =====

    /**
     * 设置/更新我的状态（不存在则插入，存在则更新）
     */
    @Insert("INSERT INTO crohn_user_status(user_id, emoji, text, description, accent, zone, updated_at) " +
            "VALUES(#{userId}, #{emoji}, #{text}, #{description}, #{accent}, #{zone}, NOW()) " +
            "ON DUPLICATE KEY UPDATE emoji=#{emoji}, text=#{text}, description=#{description}, " +
            "accent=#{accent}, zone=#{zone}, updated_at=NOW()")
    void upsertStatus(@Param("userId") Long userId,
                      @Param("emoji") String emoji,
                      @Param("text") String text,
                      @Param("description") String description,
                      @Param("accent") String accent,
                      @Param("zone") String zone);

    /**
     * 我的状态（含收到的关心数）。状态只在 24 小时内有效，过期不返回（前端回落默认）。
     */
    @Select("SELECT u.user_id AS userId, u.nickname, u.avatar, " +
            "       s.emoji, s.text, s.description, s.accent, s.zone, s.updated_at AS updatedAt, " +
            "       (SELECT COUNT(*) FROM crohn_status_reaction r WHERE r.target_user_id = u.user_id) AS reactions, " +
            "       (SELECT COUNT(*) FROM crohn_user_verification v WHERE v.user_id = u.user_id AND v.status = 'APPROVED') AS verified " +
            "FROM account_users u " +
            "LEFT JOIN crohn_user_status s ON s.user_id = u.user_id AND s.updated_at >= DATE_SUB(NOW(), INTERVAL 1 DAY) " +
            "WHERE u.user_id = #{userId}")
    StatusView findMyStatus(@Param("userId") Long userId);

    /**
     * 我所有已通过好友的状态墙（状态 24h 有效；带"我今天是否已送关心"、"是否已认证"）
     */
    @Select("SELECT u.user_id AS userId, u.nickname, u.avatar, " +
            "       s.emoji, s.text, s.description, s.accent, s.zone, s.updated_at AS updatedAt, " +
            "       (SELECT COUNT(*) FROM crohn_status_reaction r WHERE r.target_user_id = u.user_id) AS reactions, " +
            "       (SELECT COUNT(*) FROM crohn_status_reaction r2 WHERE r2.target_user_id = u.user_id AND r2.sender_id = #{myId} AND DATE(r2.created_at) = CURDATE()) AS reactedToday, " +
            "       (SELECT COUNT(*) FROM crohn_user_verification v WHERE v.user_id = u.user_id AND v.status = 'APPROVED') AS verified " +
            "FROM friendships f " +
            "JOIN account_users u ON (CASE WHEN f.requester_id = #{myId} THEN f.addressee_id ELSE f.requester_id END = u.user_id) " +
            "LEFT JOIN crohn_user_status s ON s.user_id = u.user_id AND s.updated_at >= DATE_SUB(NOW(), INTERVAL 1 DAY) " +
            "WHERE (f.requester_id = #{myId} OR f.addressee_id = #{myId}) AND f.status = 'ACCEPTED' " +
            "ORDER BY (s.updated_at IS NULL), s.updated_at DESC")
    List<StatusView> findFriendStatuses(@Param("myId") Long myId);

    /**
     * 单个战友的资料卡（状态 24h 有效 + 是否已认证 + 我今天是否已送关心）
     */
    @Select("SELECT u.user_id AS userId, u.nickname, u.avatar, " +
            "       s.emoji, s.text, s.description, s.accent, s.zone, s.updated_at AS updatedAt, " +
            "       (SELECT COUNT(*) FROM crohn_status_reaction r WHERE r.target_user_id = u.user_id) AS reactions, " +
            "       (SELECT COUNT(*) FROM crohn_status_reaction r2 WHERE r2.target_user_id = u.user_id AND r2.sender_id = #{myId} AND DATE(r2.created_at) = CURDATE()) AS reactedToday, " +
            "       (SELECT COUNT(*) FROM crohn_user_verification v WHERE v.user_id = u.user_id AND v.status = 'APPROVED') AS verified " +
            "FROM account_users u " +
            "LEFT JOIN crohn_user_status s ON s.user_id = u.user_id AND s.updated_at >= DATE_SUB(NOW(), INTERVAL 1 DAY) " +
            "WHERE u.user_id = #{targetId}")
    StatusView findUserStatusFor(@Param("targetId") Long targetId, @Param("myId") Long myId);

    /**
     * 我累计给某人送过多少份关心（不限今天）
     */
    @Select("SELECT COUNT(*) FROM crohn_status_reaction " +
            "WHERE target_user_id = #{targetId} AND sender_id = #{senderId}")
    int countReactionsTotal(@Param("senderId") Long senderId, @Param("targetId") Long targetId);

    // ===== 关心回应 =====

    @Insert("INSERT INTO crohn_status_reaction(target_user_id, sender_id, reaction_type, created_at) " +
            "VALUES(#{targetUserId}, #{senderId}, #{reactionType}, NOW())")
    void insertReaction(@Param("targetUserId") Long targetUserId,
                        @Param("senderId") Long senderId,
                        @Param("reactionType") String reactionType);

    /**
     * 我今天是否已经给 TA 送过关心
     */
    @Select("SELECT COUNT(*) FROM crohn_status_reaction " +
            "WHERE target_user_id = #{targetUserId} AND sender_id = #{senderId} AND DATE(created_at) = CURDATE()")
    int countReactionToday(@Param("targetUserId") Long targetUserId, @Param("senderId") Long senderId);

    /**
     * 最近给 TA 送关心的人（昵称+头像+类型）
     */
    @Select("SELECT u.nickname, u.avatar, r.reaction_type AS type " +
            "FROM crohn_status_reaction r JOIN account_users u ON r.sender_id = u.user_id " +
            "WHERE r.target_user_id = #{targetUserId} " +
            "ORDER BY r.created_at DESC LIMIT 8")
    List<Map<String, Object>> findReactors(@Param("targetUserId") Long targetUserId);

    // ===== 微信收款码（只存图片链接，绝不存银行卡/身份证） =====

    @Select("SELECT wechat_receive_code_url FROM crohn_user_paycode WHERE user_id = #{userId}")
    String findPayCode(@Param("userId") Long userId);

    @Insert("INSERT INTO crohn_user_paycode(user_id, wechat_receive_code_url, updated_at) " +
            "VALUES(#{userId}, #{url}, NOW()) " +
            "ON DUPLICATE KEY UPDATE wechat_receive_code_url=#{url}, updated_at=NOW()")
    void upsertPayCode(@Param("userId") Long userId, @Param("url") String url);
}
