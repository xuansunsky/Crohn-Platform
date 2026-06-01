package com.xuan.croprogram.mapper;

import com.xuan.croprogram.model.GroupMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface GroupMessageMapper {

    @Insert("INSERT INTO group_messages(group_id, sender_id, content, type) " +
            "VALUES(#{groupId}, #{senderId}, #{content}, #{type})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(GroupMessage message);

    // 群聊历史（带发送者昵称头像，按时间升序）
    @Select("SELECT gm.*, u.nickname AS senderName, u.avatar AS senderAvatar " +
            "FROM group_messages gm " +
            "LEFT JOIN users u ON gm.sender_id = u.user_id " +
            "WHERE gm.group_id = #{groupId} ORDER BY gm.created_at ASC")
    List<GroupMessage> findByGroup(@Param("groupId") Long groupId);
}
