package com.xuan.croprogram.controller;


import com.xuan.croprogram.mapper.DietReportMapper;
import com.xuan.croprogram.mapper.FoodMapper;
import com.xuan.croprogram.model.ApiResponse;
import com.xuan.croprogram.model.DietReport;
import com.xuan.croprogram.model.Food;
import com.xuan.croprogram.model.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diet")
public class DietController {

    @Autowired
    private FoodMapper foodMapper;

    @Autowired
    private DietReportMapper dietReportMapper;

    // 前端调用的“发布情报”接口
    @PostMapping("/publish")
    @Transactional // 核心！保证出错时一起回滚
    public String publishReport(
            @AuthenticationPrincipal LoginUser loginUser, // 🌟 1. 自动从Token拿当前登录大侠
            @RequestBody DietReport requestReport) {      // 🌟 2. 必须有 @RequestBody 接前端JSON

        // 1. 获取真实的 userId
        Long currentUserId = loginUser.getUserId();

        // 2. 从前端传来的“假字段”里，把名字掏出来！
        // (⚠️ 前提：你的 DietReport 实体类里已经加上了 brand, product, level 这三个假字段！)
        String brandName = requestReport.getBrand();
        String foodName = requestReport.getProduct();
        String cover_img = requestReport.getCoverImg();

        // 防呆拦截：万一前端传空了，直接打回
        if (brandName == null || foodName == null || brandName.trim().isEmpty()) {
            return "发布失败：品牌和食物名字不能为空！";
        }

        // 3. 找老祖宗 (查 Food 表)
        Food food = foodMapper.selectByBrandAndName(brandName, foodName);
        if (food == null) {
            // 没找到，说明是全网首发！新建一个
            food = new Food();
            food.setBrandName(brandName);
            food.setFoodName(foodName);
            food.setCoverImg(cover_img);
            foodMapper.insert(food);
            // 👆 因为加了 @Options，执行完这句，food.getId() 就有值了！
        }

        // 4. 记流水账 (插入 Report)
        requestReport.setFoodId(food.getId());
        requestReport.setUserId(currentUserId);
        // 🌟 把前端传过来的 level 赋值给数据库真正需要的 reactionLevel 字段
        requestReport.setReactionLevel(requestReport.getLevel());

        dietReportMapper.insert(requestReport);
        // 👆 (注意：Mapper 的 @Insert SQL 里千万别写 brand 和 product 这俩假字段，完美骗过数据库)

        // 5. 重新计算大盘数据 (更新 Food)
        food.setTotalVotes(food.getTotalVotes() + 1);

        // 根据用户的选择，给对应的等级票数 +1
        int level = requestReport.getReactionLevel();
        if (level == 1) food.setLevel1Votes(food.getLevel1Votes() + 1);
        else if (level == 2) food.setLevel2Votes(food.getLevel2Votes() + 1);
        else if (level == 3) food.setLevel3Votes(food.getLevel3Votes() + 1);
        else if (level == 4) food.setLevel4Votes(food.getLevel4Votes() + 1);
        else if (level == 5) food.setLevel5Votes(food.getLevel5Votes() + 1);
        else if (level == 6) food.setLevel6Votes(food.getLevel6Votes() + 1);

        // 6. 计算核心指标：安全率 = (绿区 + 黄区) / 总人数 * 100
        int safeCount = food.getLevel1Votes() + food.getLevel2Votes();
        int safeRate = (int) Math.round(((double) safeCount / food.getTotalVotes()) * 100);
        food.setSafeRate(safeRate);

        // 7. 更新大盘
        foodMapper.updateStats(food);

        return "发布成功！当前食物安全率：" + safeRate + "%";
    }
    // 前端调用的“获取首页情报列表”接口
    @GetMapping("/list")
    public List<Food> getFoodList() {
        return foodMapper.selectAllFoods();
    }
    @GetMapping("/reports/{foodId}")
    public ApiResponse<List<DietReport>> getReportsByFoodId(@PathVariable("foodId") Long foodId) {
        // 极简开发，直接 Mapper 一把梭！
        List<DietReport> reports = dietReportMapper.getReportsByFoodId(foodId);
        return new ApiResponse<>("发送成功", reports, 200);
    }
}