package com.xuan.croprogram.mapper;

import com.xuan.croprogram.model.Hospital;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HospitalMapper {

    @Select("SELECT * FROM hospitals ORDER BY rating DESC")
    List<Hospital> findAll();

    @Select("SELECT * FROM hospitals WHERE region = #{region} ORDER BY rating DESC")
    List<Hospital> findByRegion(String region);
}
