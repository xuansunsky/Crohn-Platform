package com.xuan.croprogram.mapper;

import com.xuan.croprogram.model.SquadTask;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SquadTaskMapper {

    @Insert("INSERT INTO squad_tasks(group_id, label, owner_id, assignee_id, priority, done) " +
            "VALUES(#{groupId}, #{label}, #{ownerId}, #{assigneeId}, #{priority}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(SquadTask task);

    @Select("SELECT t.*, o.nickname AS ownerName, a.nickname AS assigneeName FROM squad_tasks t " +
            "LEFT JOIN account_users o ON t.owner_id = o.user_id " +
            "LEFT JOIN account_users a ON t.assignee_id = a.user_id " +
            "WHERE t.group_id = #{groupId} ORDER BY t.done ASC, t.created_at DESC")
    List<SquadTask> findByGroup(@Param("groupId") Long groupId);

    @Select("SELECT * FROM squad_tasks WHERE id = #{id}")
    SquadTask findById(@Param("id") Long id);

    @Update("UPDATE squad_tasks SET done = #{done} WHERE id = #{id}")
    void updateDone(@Param("id") Long id, @Param("done") Integer done);

    @Update("UPDATE squad_tasks SET assignee_id = #{assigneeId} WHERE id = #{id}")
    void updateAssignee(@Param("id") Long id, @Param("assigneeId") Long assigneeId);
}
