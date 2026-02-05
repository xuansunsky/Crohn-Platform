package com.xuan.croprogram.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuan.croprogram.mapper.PolicyMapper;
import com.xuan.croprogram.model.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/policy")
public class PolicyController {

    @Autowired
    private PolicyMapper policyMapper;

    // JSON 转换工具
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ✅ 1. 默认详情接口 (进页面自动加载最好的)
    @GetMapping("/detail")
    public Map<String, Object> getDetail(@RequestParam("city") String city,
                                         @RequestParam("type") String type) {
        Map<String, Object> result = new HashMap<>();
        try {
            Policy policy = policyMapper.selectBestPolicy(city, type);
            if (policy != null) {
                // 核心：把数据库里的 JSON 字符串转成 List 给前端
                handleJsonFields(policy);
            }
            result.put("code", 200);
            result.put("data", policy); // 如果没数据就是 null，前端处理一下
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "查询失败");
        }
        return result;
    }

    // ✅ 2. 抽屉历史列表接口
    @GetMapping("/history")
    public Map<String, Object> getHistory(@RequestParam("city") String city,
                                          @RequestParam("type") String type) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Policy> list = policyMapper.selectHistoryList(city, type);
            // 也要处理一下图片 JSON，因为列表里可能要显示“含3张图”
            for (Policy p : list) {
                handleJsonFields(p);
            }
            result.put("code", 200);
            result.put("data", list);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取历史失败");
        }
        return result;
    }

    // ✅ 3. 切换版本接口 (通过 ID 查)
    @GetMapping("/detail/version")
    public Map<String, Object> getVersion(@RequestParam("id") Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            Policy policy = policyMapper.selectById(id);
            if (policy != null) {
                handleJsonFields(policy);
            }
            result.put("code", 200);
            result.put("data", policy);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "切换版本失败");
        }
        return result;
    }

    // 🔧 工具方法：把 JSON 字符串剥壳成 List
    // 兄弟，这个方法复用率很高，放在这里专门干脏活
    private void handleJsonFields(Policy policy) {
        try {
            // 1. 转图片
            if (policy.getEvidenceImgs() != null && !policy.getEvidenceImgs().isEmpty()) {
                List<String> imgList = objectMapper.readValue(
                        policy.getEvidenceImgs(), new TypeReference<List<String>>() {});
                policy.setEvidenceList(imgList);
            } else {
                policy.setEvidenceList(new ArrayList<>());
            }

            // 2. 转药品
            if (policy.getDrugsJson() != null && !policy.getDrugsJson().isEmpty()) {
                List<Policy.DrugItem> drugList = objectMapper.readValue(
                        policy.getDrugsJson(), new TypeReference<List<Policy.DrugItem>>() {});
                policy.setDrugs(drugList);
            } else {
                policy.setDrugs(new ArrayList<>());
            }
        } catch (Exception e) {
            System.out.println("JSON解析出错了，但不影响主流程: " + e.getMessage());
        }
    }
    @PostMapping("/like")
    public Map<String, Object> like(@RequestParam("id") Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            policyMapper.incrementLikes(id);
            result.put("code", 200);
            result.put("msg", "点赞成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "点赞失败");
        }
        return result;
    }
    // ✅ 2. 提交/保存接口 (POST)
    @PostMapping("/save")
    public Map<String, Object> save(@RequestBody Policy policy) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 2.1 处理药品 (List -> String)
            if (policy.getDrugs() != null) {
                String jsonStr = objectMapper.writeValueAsString(policy.getDrugs());
                policy.setDrugsJson(jsonStr);
            }

            // 🔥 2.2 处理证据图片 (List -> String)
            // 注意：这里全是小写开头！getEvidenceList -> setEvidenceImgs
            if (policy.getEvidenceList() != null && !policy.getEvidenceList().isEmpty()) {
                String evidenceJson = objectMapper.writeValueAsString(policy.getEvidenceList());
                policy.setEvidenceImgs(evidenceJson);
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