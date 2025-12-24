package com.xuan.croprogram.controller;

import com.xuan.croprogram.mapper.MedicalPolicyMapper;
import com.xuan.croprogram.model.ApiResponse;
import com.xuan.croprogram.model.MedicalPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/policy")
public class MedicalPolicyController {

    @Autowired
    private MedicalPolicyMapper mapper; // 直接指挥库管

    // 🚰 查询接口
    @GetMapping("/query")
    public ApiResponse<MedicalPolicy> getPolicy(@RequestParam String city, @RequestParam String type) {
        // 1. 直接让 Mapper 去挖数据
        MedicalPolicy policy = mapper.findByCityAndType(city, type);

        // 2. 判空逻辑
        if (policy == null) {
            // 这里 status 404 表示没找到，message 给前端提示
            return new ApiResponse<>("暂无数据，快去贡献！", null, 404);
        }

        // 3. 找到了，封装返回
        return new ApiResponse<>("查询成功", policy, 200);
    }

    // 📥 保存接口
    @PostMapping("/save")
    public ApiResponse<String> savePolicy(@RequestBody MedicalPolicy policy) {
        // 1. 查重
        MedicalPolicy exist = mapper.findByCityAndType(policy.getCityCode(), policy.getPolicyType());

        if (exist == null) {
            mapper.insert(policy); // 没数据就插
        } else {
            mapper.update(policy); // 有数据就改
        }

        // 2. 返回成功
        return new ApiResponse<>("King的旨意已送达数据库！", null, 200);
    }
}