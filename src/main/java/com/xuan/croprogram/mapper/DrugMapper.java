package com.xuan.croprogram.mapper;

import com.xuan.croprogram.model.Drug;
import com.xuan.croprogram.model.UserCabinet;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DrugMapper {

    @Select("SELECT * FROM medical_drugs ORDER BY safety_rate DESC")
    List<Drug> findAll();

    @Select("SELECT * FROM medical_user_cabinet WHERE user_id = #{userId}")
    List<UserCabinet> findCabinetByUserId(Long userId);

    @Insert("INSERT INTO medical_user_cabinet(user_id, drug_name, drug_icon, dosage, frequency, time_of_day) VALUES(#{userId}, #{drugName}, #{drugIcon}, #{dosage}, #{frequency}, #{timeOfDay})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertCabinet(UserCabinet cabinet);

    @Delete("DELETE FROM medical_user_cabinet WHERE id = #{id} AND user_id = #{userId}")
    void deleteCabinet(@Param("id") Long id, @Param("userId") Long userId);
}
