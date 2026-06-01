package com.xuan.croprogram.mapper;

import com.xuan.croprogram.model.SquadActivity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SquadActivityMapper {

    @Insert("INSERT INTO squad_activities(group_id, user_id, action) " +
            "VALUES(#{groupId}, #{userId}, #{action})")
    void insert(SquadActivity activity);

    @Select("SELECT a.*, u.nickname FROM squad_activities a " +
            "LEFT JOIN users u ON a.user_id = u.user_id " +
            "WHERE a.group_id = #{groupId} ORDER BY a.created_at DESC LIMIT 20")
    List<SquadActivity> findByGroup(@Param("groupId") Long groupId);
}
