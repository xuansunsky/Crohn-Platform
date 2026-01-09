package com.xuan.croprogram.mapper;

import com.xuan.croprogram.model.Message;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import java.util.List;

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
    List<Message> findChatHistory(Long myId, Long friendId);


    // @Update("UPDATE messages SET is_read = 1 WHERE sender_id = #{friendId} AND receiver_id = #{myId}")
    // void markAsRead(Long myId, Long friendId);
}