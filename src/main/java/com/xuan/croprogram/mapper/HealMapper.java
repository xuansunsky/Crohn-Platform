package com.xuan.croprogram.mapper;

import com.xuan.croprogram.model.PainSignal;
import com.xuan.croprogram.model.PaperBoat;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface HealMapper {

    // --- 用户暖心值 ---
    @Select("SELECT warmth_points FROM crohn_user_warmth WHERE user_id = #{userId}")
    Integer findWarmthPoints(Long userId);

    @Insert("INSERT INTO crohn_user_warmth(user_id, warmth_points) VALUES(#{userId}, #{points}) ON DUPLICATE KEY UPDATE warmth_points = #{points}")
    void saveWarmthPoints(@Param("userId") Long userId, @Param("points") int points);

    @Update("UPDATE crohn_user_warmth SET warmth_points = warmth_points + #{points} WHERE user_id = #{userId}")
    void addWarmthPoints(@Param("userId") Long userId, @Param("points") int points);

    // --- 痛痛呼救信号 ---
    @Insert("INSERT INTO crohn_pain_signal(user_id, location, status, created_at) VALUES(#{userId}, #{location}, 1, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertPainSignal(PainSignal signal);

    @Update("UPDATE crohn_pain_signal SET status = 0 WHERE user_id = #{userId} AND status = 1")
    void deactivatePainSignal(Long userId);

    @Select("SELECT s.*, u.nickname as name, u.avatar FROM crohn_pain_signal s LEFT JOIN users u ON s.user_id = u.user_id WHERE s.status = 1 AND s.user_id != #{userId} ORDER BY s.created_at DESC")
    List<PainSignal> findActivePainSignals(Long userId);

    // --- 暖心安慰 ---
    @Insert("INSERT IGNORE INTO crohn_pain_comfort(signal_id, comforter_id, comfort_type, created_at) VALUES(#{signalId}, #{comforterId}, #{comfortType}, NOW())")
    int insertComfort(@Param("signalId") Long signalId, @Param("comforterId") Long comforterId, @Param("comfortType") String comfortType);

    @Select("SELECT COUNT(*) FROM crohn_pain_comfort WHERE signal_id = #{signalId} AND comforter_id = #{comforterId}")
    int checkComforted(@Param("signalId") Long signalId, @Param("comforterId") Long comforterId);

    // --- 情绪解压纸船 ---
    @Insert("INSERT INTO crohn_paper_boat(content, breeze_count, created_at) VALUES(#{content}, 0, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertPaperBoat(PaperBoat boat);

    @Select("SELECT * FROM crohn_paper_boat ORDER BY RAND() LIMIT 1")
    PaperBoat findRandomPaperBoat();

    @Insert("INSERT IGNORE INTO crohn_paper_boat_breeze(boat_id, user_id, created_at) VALUES(#{boatId}, #{userId}, NOW())")
    int insertBreeze(@Param("boatId") Long boatId, @Param("userId") Long userId);

    @Update("UPDATE crohn_paper_boat SET breeze_count = breeze_count + 1 WHERE id = #{boatId}")
    void incrementBreeze(Long boatId);
}
