package com.xuan.croprogram.mapper;

import com.xuan.croprogram.model.ChatGroup;
import com.xuan.croprogram.model.GroupMember;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface GroupMapper {

    // 建群
    @Insert("INSERT INTO chat_groups(name, avatar, notice, owner_id) " +
            "VALUES(#{name}, #{avatar}, #{notice}, #{ownerId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertGroup(ChatGroup group);

    // 加成员（重复忽略）
    @Insert("INSERT IGNORE INTO group_members(group_id, user_id, role) VALUES(#{groupId}, #{userId}, #{role})")
    void insertMember(@Param("groupId") Long groupId, @Param("userId") Long userId, @Param("role") String role);

    // 我加入的所有群（带成员数）
    @Select("SELECT g.*, (SELECT COUNT(*) FROM group_members m2 WHERE m2.group_id = g.id) AS memberCount " +
            "FROM chat_groups g " +
            "JOIN group_members m ON g.id = m.group_id " +
            "WHERE m.user_id = #{userId} " +
            "ORDER BY g.created_at DESC")
    List<ChatGroup> findMyGroups(@Param("userId") Long userId);

    // 群详情
    @Select("SELECT g.*, (SELECT COUNT(*) FROM group_members m WHERE m.group_id = g.id) AS memberCount " +
            "FROM chat_groups g WHERE g.id = #{id}")
    ChatGroup findById(@Param("id") Long id);

    // 群成员（带昵称头像）
    @Select("SELECT m.*, u.nickname, u.avatar FROM group_members m " +
            "LEFT JOIN users u ON m.user_id = u.user_id " +
            "WHERE m.group_id = #{groupId} ORDER BY m.joined_at ASC")
    List<GroupMember> findMembers(@Param("groupId") Long groupId);

    // 是否群成员
    @Select("SELECT COUNT(*) FROM group_members WHERE group_id = #{groupId} AND user_id = #{userId}")
    int isMember(@Param("groupId") Long groupId, @Param("userId") Long userId);
}
