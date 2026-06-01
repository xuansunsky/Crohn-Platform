package com.xuan.croprogram.controller;

import com.xuan.croprogram.mapper.HospitalMapper;
import com.xuan.croprogram.model.ApiResponse;
import com.xuan.croprogram.model.Hospital;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospital")
public class HospitalController {

    @Autowired
    private HospitalMapper hospitalMapper;

    @GetMapping("/list")
    public ApiResponse<List<Hospital>> list(@RequestParam(required = false) String region) {
        List<Hospital> list = region != null ? hospitalMapper.findByRegion(region) : hospitalMapper.findAll();
        return new ApiResponse<>("获取成功", list, 200);
    }
}
