package com.xuan.croprogram.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SquadTask {
    private Long id;
    private Long groupId;
    private String label;
    private Long ownerId;
    private Long assigneeId;
    private String priority;
    private Integer done;
    private LocalDateTime createdAt;
    // from JOIN with users
    private String ownerName;
    private String assigneeName;
}


