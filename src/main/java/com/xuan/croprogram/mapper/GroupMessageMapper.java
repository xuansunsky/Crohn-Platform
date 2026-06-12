package com.xuan.croprogram.mapper;

import com.xuan.croprogram.model.GroupMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface GroupMessageMapper {

    @Insert("INSERT INTO group_messages(group_id, sender_id, content, type) " +
            "VALUES(#{groupId}, #{senderId}, #{content}, #{type})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(GroupMessage message);

    // 群聊历史（带发送者昵称头像，按时间升序）
    @Select("SELECT gm.*, u.nickname AS senderName, u.avatar AS senderAvatar " +
            "FROM group_messages gm " +
            "LEFT JOIN account_users u ON gm.sender_id = u.user_id " +
            "WHERE gm.group_id = #{groupId} ORDER BY gm.created_at ASC")
    List<GroupMessage> findByGroup(@Param("groupId") Long groupId);

    @Select("SELECT 'group' AS type, " +
            "g.id AS id, " +
            "g.name AS name, " +
            "g.avatar AS avatar, " +
            "gm.content AS lastMsg, " +
            "DATE_FORMAT(gm.created_at, '%m-%d %H:%i') AS lastTime, " +
            "gm.created_at AS matchedAt " +
            "FROM group_messages gm " +
            "JOIN ( " +
            "  SELECT MAX(gm2.id) AS id " +
            "  FROM group_messages gm2 " +
            "  JOIN group_members gm_member ON gm_member.group_id = gm2.group_id AND gm_member.user_id = #{myId} " +
            "  WHERE LOWER(COALESCE(gm2.type, 'text')) != 'image' " +
            "    AND gm2.content LIKE CONCAT('%', #{keyword}, '%') " +
            "  GROUP BY gm2.group_id " +
            ") hit ON hit.id = gm.id " +
            "JOIN chat_groups g ON g.id = gm.group_id " +
            "ORDER BY gm.created_at DESC " +
            "LIMIT 30")
    List<Map<String, Object>> searchConversationMatches(@Param("myId") Long myId, @Param("keyword") String keyword);
}
