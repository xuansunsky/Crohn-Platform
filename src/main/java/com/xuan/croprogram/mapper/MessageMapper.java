package com.xuan.croprogram.mapper;

import com.xuan.croprogram.model.Message;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;
import java.util.Map;

@Mapper
public interface MessageMapper {

    // 1. 【发信】把消息塞进信箱
    @Insert("INSERT INTO messages(sender_id, receiver_id, content, type, is_read, created_at) " +
            "VALUES(#{senderId}, #{receiverId}, #{content}, #{type}, 0, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Message message);

    // 2. 【读信】查我和某人的聊天记录
    // 逻辑：(是我发的 给他的) OR (是他发的 给我的) -> 按时间排序
    @Select("SELECT * FROM messages WHERE " +
            "(sender_id = #{myId} AND receiver_id = #{friendId}) OR " +
            "(sender_id = #{friendId} AND receiver_id = #{myId}) " +
            "ORDER BY created_at ASC")
    List<Message> findChatHistory(@Param("myId") Long myId, @Param("friendId") Long friendId);


    @Update("UPDATE messages SET is_read = 1 WHERE sender_id = #{friendId} AND receiver_id = #{myId}")
    void markAsRead(@Param("myId") Long myId, @Param("friendId") Long friendId);

    @Select("SELECT 'single' AS type, " +
            "IF(m.sender_id = #{myId}, m.receiver_id, m.sender_id) AS id, " +
            "u.nickname AS name, " +
            "u.avatar AS avatar, " +
            "m.content AS lastMsg, " +
            "DATE_FORMAT(m.created_at, '%m-%d %H:%i') AS lastTime, " +
            "m.created_at AS matchedAt " +
            "FROM messages m " +
            "JOIN ( " +
            "  SELECT MAX(id) AS id " +
            "  FROM messages " +
            "  WHERE (sender_id = #{myId} OR receiver_id = #{myId}) " +
            "    AND LOWER(COALESCE(type, 'text')) != 'image' " +
            "    AND content LIKE CONCAT('%', #{keyword}, '%') " +
            "  GROUP BY IF(sender_id = #{myId}, receiver_id, sender_id) " +
            ") hit ON hit.id = m.id " +
            "JOIN users u ON u.user_id = IF(m.sender_id = #{myId}, m.receiver_id, m.sender_id) " +
            "ORDER BY m.created_at DESC " +
            "LIMIT 30")
    List<Map<String, Object>> searchConversationMatches(@Param("myId") Long myId, @Param("keyword") String keyword);
}
