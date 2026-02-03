package com.xuan.croprogram.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuan.croprogram.mapper.PolicyMapper;
import com.xuan.croprogram.model.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/policy")
@CrossOrigin
public class PolicyController {

    @Autowired
    private PolicyMapper policyMapper;

    // JSON 转换工具
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ✅ 1. 查询接口 (GET)
    @GetMapping("/detail")
    public Map<String, Object> getDetail(@RequestParam("city") String city,
                                         @RequestParam("type") String type) {
        Map<String, Object> result = new HashMap<>();
        try {
            Policy policy = policyMapper.selectOnePolicy(city, type);

            if (policy != null && policy.getDrugsJson() != null) {
                // 🔥 关键步骤：把数据库取出来的 String 变成 List 给前端
                List<Policy.DrugItem> drugList = objectMapper.readValue(
                        policy.getDrugsJson(),
                        new TypeReference<List<Policy.DrugItem>>() {}
                );
                policy.setDrugs(drugList);
            }

            result.put("code", 200);
            result.put("data", policy); // policy 为 null 前端也能处理

        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "查询失败");
        }
        return result;
    }

    // ✅ 2. 提交/保存接口 (POST) - 包含了新增和修改
    @PostMapping("/save")
    public Map<String, Object> save(@RequestBody Policy policy) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 🔥 关键步骤：把前端传来的 List 变成 String 存数据库
            if (policy.getDrugs() != null) {
                String jsonStr = objectMapper.writeValueAsString(policy.getDrugs());
                policy.setDrugsJson(jsonStr);
            }

            // 判断是新增还是更新
            int count = policyMapper.count(policy.getCityName(), policy.getPolicyType());
            if (count > 0) {
                policyMapper.update(policy);
            } else {
                policyMapper.insert(policy);
            }

            result.put("code", 200);
            result.put("msg", "保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "保存失败: " + e.getMessage());
        }
        return result;
    }
}