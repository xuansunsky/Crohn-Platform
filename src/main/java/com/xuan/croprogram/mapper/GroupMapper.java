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
    @Select("SELECT g.*, " +
            "(SELECT COUNT(*) FROM group_members m2 WHERE m2.group_id = g.id) AS memberCount, " +
            "(SELECT CASE WHEN gm.type = 'image' THEN '[图片]' ELSE gm.content END " +
            " FROM group_messages gm WHERE gm.group_id = g.id ORDER BY gm.created_at DESC LIMIT 1) AS lastMsg, " +
            "(SELECT DATE_FORMAT(gm.created_at, '%m-%d %H:%i') " +
            " FROM group_messages gm WHERE gm.group_id = g.id ORDER BY gm.created_at DESC LIMIT 1) AS lastTime " +
            "FROM chat_groups g " +
            "JOIN group_members m ON g.id = m.group_id " +
            "WHERE m.user_id = #{userId} " +
            "ORDER BY COALESCE((SELECT MAX(gm.created_at) FROM group_messages gm WHERE gm.group_id = g.id), g.created_at) DESC")
    List<ChatGroup> findMyGroups(@Param("userId") Long userId);

    // 群详情
    @Select("SELECT g.*, (SELECT COUNT(*) FROM group_members m WHERE m.group_id = g.id) AS memberCount " +
            "FROM chat_groups g WHERE g.id = #{id}")
    ChatGroup findById(@Param("id") Long id);

    @Update("UPDATE chat_groups SET name = #{name}, avatar = #{avatar}, notice = #{notice} WHERE id = #{id}")
    void updateGroup(ChatGroup group);

    // 群成员（带昵称头像）
    @Select("SELECT m.*, u.nickname, u.avatar FROM group_members m " +
            "LEFT JOIN account_users u ON m.user_id = u.user_id " +
            "WHERE m.group_id = #{groupId} ORDER BY m.joined_at ASC")
    List<GroupMember> findMembers(@Param("groupId") Long groupId);

    // 是否群成员
    @Select("SELECT COUNT(*) FROM group_members WHERE group_id = #{groupId} AND user_id = #{userId}")
    int isMember(@Param("groupId") Long groupId, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM group_members mine " +
            "JOIN group_members target ON mine.group_id = target.group_id " +
            "WHERE mine.user_id = #{viewerId} AND target.user_id = #{targetId}")
    int countSharedGroups(@Param("viewerId") Long viewerId, @Param("targetId") Long targetId);

    @Select("SELECT role FROM group_members WHERE group_id = #{groupId} AND user_id = #{userId} LIMIT 1")
    String findMemberRole(@Param("groupId") Long groupId, @Param("userId") Long userId);

    @Delete("DELETE FROM group_members WHERE group_id = #{groupId} AND user_id = #{userId}")
    int deleteMember(@Param("groupId") Long groupId, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM group_members WHERE group_id = #{groupId}")
    int countMembers(@Param("groupId") Long groupId);

    @Delete("DELETE FROM chat_groups WHERE id = #{groupId}")
    void deleteGroup(@Param("groupId") Long groupId);
}
