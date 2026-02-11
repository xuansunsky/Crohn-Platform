package com.xuan.croprogram.model;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExperiencePost {

    @TableId(type = IdType.AUTO)
    private Long id;

    // 关联的用户ID
    private Long userId;

    // 这里的 updatable = false 意味着：
    // 这个字段只在 insert 时写入，update 时如果你没传也不会被覆盖为 null
    // 但通常我们会用连表查询来获取作者昵称，这里先只存 ID

    private String title;

    private String summary;

    private String icon;

    private String theme;

    private String tags; // 存 "标签1,标签2"

    private LocalDateTime createdAt;

    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}