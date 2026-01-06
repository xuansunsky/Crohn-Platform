package com.xuan.croprogram.mapper;

import com.xuan.croprogram.model.ExperiencePost;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ExperiencePostMapper {

    // 1. 查所有帖子 (倒序，最新的在前面)
    @Select("SELECT * FROM experience_posts ORDER BY created_at DESC")
    List<ExperiencePost> findAll();

    // 2. 新增帖子
    @Insert("INSERT INTO experience_posts(user_id, title, summary, icon, theme, tags, created_at) " +
            "VALUES(#{userId}, #{title}, #{summary}, #{icon}, #{theme}, #{tags}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ExperiencePost post);

    // 3. 删除帖子 (根据ID)
    @Delete("DELETE FROM experience_posts WHERE id = #{id}")
    void deleteById(Long id);

    // 4. 查单个帖子 (为了删除前鉴权，看看是不是本人写的)
    @Select("SELECT * FROM experience_posts WHERE id = #{id}")
    ExperiencePost findById(Long id);

    @Update("UPDATE experience_posts SET " +
            "title = #{title}, " +
            "summary = #{summary}, " +
            "icon = #{icon}, " +
            "theme = #{theme}, " +
            "tags = #{tags} " +
            "WHERE id = #{id}") // 👈 这一句是保命的！只改这一条！
    void update(ExperiencePost post);
}