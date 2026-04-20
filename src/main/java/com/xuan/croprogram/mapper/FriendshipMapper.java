package com.xuan.croprogram.mapper;

import com.xuan.croprogram.model.FriendDto;
import com.xuan.croprogram.model.Friendship;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface FriendshipMapper {

    // 1. 【安检】检查两个人有没有关系 (不管谁加的谁)
    @Select("SELECT * FROM friendships WHERE " +
            "(requester_id = #{uid1} AND addressee_id = #{uid2}) OR " +
            "(requester_id = #{uid2} AND addressee_id = #{uid1}) LIMIT 1")
    Friendship findRelation(Long uid1, Long uid2);

    // 2. 【下单】插入一条新的好友申请
    @Insert("INSERT INTO friendships(requester_id, addressee_id, status, created_at) " +
            "VALUES(#{requesterId}, #{addresseeId}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Friendship friendship);

    // 3. 🔥【取货】查我的好友列表 (返回 DTO 大礼包)
    // 逻辑：只要状态是 ACCEPTED，且我是其中一方，就把对方的信息查出来
    @Select("SELECT " +
            "  u.user_id as friendId, " +
            "  u.nickname, " +
            "  u.avatar, " +
            "  f.id as friendshipId, " +
            "  f.status " +
            "FROM friendships f " +
            "JOIN users u ON (CASE WHEN f.requester_id = #{myId} THEN f.addressee_id ELSE f.requester_id END = u.user_id) " +
            "WHERE (f.requester_id = #{myId} OR f.addressee_id = #{myId}) " +
            "AND f.status = 'ACCEPTED'")
    List<FriendDto> findMyFriends(Long myId);

    // 4. 【信箱】查谁申请加我 (Pending 状态)
    @Select("SELECT " +
            "  u.user_id as friendId, " +
            "  u.nickname, " +
            "  u.avatar, " +
            "  f.id as friendshipId, " +
            "  f.status " +
            "FROM friendships f " +
            "JOIN users u ON f.requester_id = u.user_id " +
            "WHERE f.addressee_id = #{myId} AND f.status = 'PENDING'")
    List<FriendDto> findPendingRequests(Long myId);

    // 5. 根据ID查记录 (为了后面同意申请时做检查)
    @Select("SELECT * FROM friendships WHERE id = #{id}")
    Friendship findById(Long id);

    // 6. 【盖章】更新状态 (同意/拒绝/拉黑)
    @Update("UPDATE friendships SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    void updateStatus(Long id, String status);
}