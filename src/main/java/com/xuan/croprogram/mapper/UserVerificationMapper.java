package com.xuan.croprogram.mapper;

import com.xuan.croprogram.model.UserVerification;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserVerificationMapper {

    /**
     * 提交认证：当前阶段「上传即解锁」，直接置为 APPROVED（管理员审核功能保留，后续可切回 PENDING 复审）。
     */
    @Insert("INSERT INTO crohn_user_verification(user_id, proof_image_url, status, created_at, reviewed_at) " +
            "VALUES(#{userId}, #{proofImageUrl}, 'APPROVED', NOW(), NOW()) " +
            "ON DUPLICATE KEY UPDATE proof_image_url=#{proofImageUrl}, status='APPROVED', created_at=NOW(), reviewed_at=NOW()")
    void submit(@Param("userId") Long userId, @Param("proofImageUrl") String proofImageUrl);

    @Select("SELECT * FROM crohn_user_verification WHERE user_id = #{userId}")
    UserVerification findByUser(@Param("userId") Long userId);

    /**
     * 待审核列表（管理员）
     */
    @Select("SELECT v.*, u.nickname, u.avatar FROM crohn_user_verification v " +
            "LEFT JOIN account_users u ON v.user_id = u.user_id " +
            "WHERE v.status = 'PENDING' ORDER BY v.created_at ASC")
    List<UserVerification> findPending(@Param("status") String status);

    @Update("UPDATE crohn_user_verification SET status = #{status}, reviewed_at = NOW() WHERE user_id = #{userId}")
    void review(@Param("userId") Long userId, @Param("status") String status);
}
