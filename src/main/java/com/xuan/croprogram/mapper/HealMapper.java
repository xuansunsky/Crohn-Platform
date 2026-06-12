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

    @Select("SELECT s.*, u.nickname as name, u.avatar FROM crohn_pain_signal s LEFT JOIN account_users u ON s.user_id = u.user_id WHERE s.status = 1 AND s.user_id != #{userId} ORDER BY s.created_at DESC")
    List<PainSignal> findActivePainSignals(Long userId);

    // --- 暖心安慰 ---
    @Insert("INSERT IGNORE INTO crohn_pain_comfort(signal_id, comforter_id, comfort_type, created_at) VALUES(#{signalId}, #{comforterId}, #{comfortType}, NOW())")
    int insertComfort(@Param("signalId") Long signalId, @Param("comforterId") Long comforterId, @Param("comfortType") String comfortType);

    @Select("SELECT COUNT(*) FROM crohn_pain_comfort WHERE signal_id = #{signalId} AND comforter_id = #{comforterId}")
    int checkComforted(@Param("signalId") Long signalId, @Param("comforterId") Long comforterId);

    // --- 情绪解压纸船 ---
    @Insert("INSERT INTO crohn_paper_boat(user_id, content, breeze_count, created_at) VALUES(#{userId}, #{content}, 0, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertPaperBoat(PaperBoat boat);

    @Select("""
            SELECT
              b.*,
              (SELECT COUNT(*) FROM crohn_paper_boat_response r WHERE r.boat_id = b.id AND r.response_type = 'reply') AS reply_count,
              (SELECT COUNT(*) FROM crohn_paper_boat_response r WHERE r.boat_id = b.id AND r.response_type = 'gift') AS gift_count,
              EXISTS(SELECT 1 FROM crohn_paper_boat_breeze br WHERE br.boat_id = b.id AND br.user_id = #{userId}) AS breezed,
              EXISTS(SELECT 1 FROM crohn_paper_boat_response r WHERE r.boat_id = b.id AND r.user_id = #{userId} AND r.response_type = 'reply') AS replied,
              EXISTS(SELECT 1 FROM crohn_paper_boat_response r WHERE r.boat_id = b.id AND r.user_id = #{userId} AND r.response_type = 'gift') AS gifted
            FROM crohn_paper_boat b
            WHERE (b.user_id IS NULL OR b.user_id != #{userId})
              AND NOT EXISTS (
                SELECT 1 FROM crohn_paper_boat_scoop s
                WHERE s.boat_id = b.id AND s.user_id = #{userId}
              )
            ORDER BY RAND()
            LIMIT 1
            """)
    PaperBoat findRandomPaperBoat(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM crohn_paper_boat WHERE user_id = #{userId} AND DATE(created_at) = CURDATE()")
    int countPaperBoatReleasedToday(Long userId);

    @Select("SELECT COUNT(*) FROM crohn_paper_boat_scoop WHERE user_id = #{userId} AND DATE(created_at) = CURDATE()")
    int countPaperBoatScoopedToday(Long userId);

    @Select("""
            SELECT
              b.*,
              (SELECT COUNT(*) FROM crohn_paper_boat_response r WHERE r.boat_id = b.id AND r.response_type = 'reply') AS reply_count,
              (SELECT COUNT(*) FROM crohn_paper_boat_response r WHERE r.boat_id = b.id AND r.response_type = 'gift') AS gift_count,
              EXISTS(SELECT 1 FROM crohn_paper_boat_breeze br WHERE br.boat_id = b.id AND br.user_id = #{userId}) AS breezed
            FROM crohn_paper_boat b
            WHERE b.user_id = #{userId}
            ORDER BY b.created_at DESC
            LIMIT 50
            """)
    List<PaperBoat> findMyReleasedPaperBoats(@Param("userId") Long userId);

    @Select("""
            SELECT
              b.*,
              s.created_at AS scooped_at,
              (SELECT COUNT(*) FROM crohn_paper_boat_response r WHERE r.boat_id = b.id AND r.response_type = 'reply') AS reply_count,
              (SELECT COUNT(*) FROM crohn_paper_boat_response r WHERE r.boat_id = b.id AND r.response_type = 'gift') AS gift_count,
              EXISTS(SELECT 1 FROM crohn_paper_boat_breeze br WHERE br.boat_id = b.id AND br.user_id = #{userId}) AS breezed,
              EXISTS(SELECT 1 FROM crohn_paper_boat_response r WHERE r.boat_id = b.id AND r.user_id = #{userId} AND r.response_type = 'reply') AS replied,
              EXISTS(SELECT 1 FROM crohn_paper_boat_response r WHERE r.boat_id = b.id AND r.user_id = #{userId} AND r.response_type = 'gift') AS gifted,
              (SELECT r.content FROM crohn_paper_boat_response r WHERE r.boat_id = b.id AND r.user_id = #{userId} AND r.response_type = 'reply' ORDER BY r.created_at DESC LIMIT 1) AS reply_content,
              (SELECT r.gift_type FROM crohn_paper_boat_response r WHERE r.boat_id = b.id AND r.user_id = #{userId} AND r.response_type = 'gift' ORDER BY r.created_at DESC LIMIT 1) AS gift_type
            FROM crohn_paper_boat_scoop s
            JOIN crohn_paper_boat b ON b.id = s.boat_id
            WHERE s.user_id = #{userId}
            ORDER BY s.created_at DESC
            LIMIT 50
            """)
    List<PaperBoat> findMyScoopedPaperBoats(@Param("userId") Long userId);

    @Insert("INSERT INTO crohn_paper_boat_scoop(boat_id, user_id, created_at) VALUES(#{boatId}, #{userId}, NOW())")
    void insertPaperBoatScoop(@Param("boatId") Long boatId, @Param("userId") Long userId);

    @Insert("""
            INSERT IGNORE INTO crohn_paper_boat_response(boat_id, user_id, response_type, content, gift_type, created_at)
            VALUES(#{boatId}, #{userId}, #{responseType}, #{content}, #{giftType}, NOW())
            """)
    int insertPaperBoatResponse(
            @Param("boatId") Long boatId,
            @Param("userId") Long userId,
            @Param("responseType") String responseType,
            @Param("content") String content,
            @Param("giftType") String giftType
    );

    @Insert("INSERT IGNORE INTO crohn_paper_boat_breeze(boat_id, user_id, created_at) VALUES(#{boatId}, #{userId}, NOW())")
    int insertBreeze(@Param("boatId") Long boatId, @Param("userId") Long userId);

    @Update("UPDATE crohn_paper_boat SET breeze_count = breeze_count + 1 WHERE id = #{boatId}")
    void incrementBreeze(Long boatId);
}
