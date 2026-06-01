package com.xuan.croprogram.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * IBD 病友身份认证：上传确诊单/肠镜/药单等证明（建议涂黑姓名医院），后台审核
 */
@Data
public class UserVerification {
    private Long userId;
    // 证明图片链接（仅图片，绝不存身份证/银行卡）
    private String proofImageUrl;
    // PENDING / APPROVED / REJECTED
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    // 连表带出的申请人信息（不入库）
    private String nickname;
    private String avatar;
}
