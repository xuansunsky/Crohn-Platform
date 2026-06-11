package com.xuan.croprogram.controller;


import com.xuan.croprogram.mapper.DietReportMapper;
import com.xuan.croprogram.mapper.FoodMapper;
import com.xuan.croprogram.model.ApiResponse;
import com.xuan.croprogram.model.DietReport;
import com.xuan.croprogram.model.DietReportComment;
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
    public ApiResponse<Food> publishReport(
            @AuthenticationPrincipal LoginUser loginUser, // 🌟 1. 自动从Token拿当前登录大侠
            @RequestBody DietReport requestReport) {      // 🌟 2. 必须有 @RequestBody 接前端JSON

        // 1. 获取真实的 userId
        Long currentUserId = loginUser.getUserId();

        // 2. 从前端传来的“假字段”里，把名字掏出来！
        // (⚠️ 前提：你的 DietReport 实体类里已经加上了 brand, product, level 这三个假字段！)
        String brandName = requestReport.getBrand() == null ? "" : requestReport.getBrand().trim();
        String foodName = requestReport.getProduct() == null ? "" : requestReport.getProduct().trim();
        String coverImg = requestReport.getCoverImg() == null ? "" : requestReport.getCoverImg().trim();

        // 防呆拦截：万一前端传空了，直接打回
        if (brandName.length() < 2 || foodName.length() < 2) {
            return new ApiResponse<>("品牌和食物名字至少写 2 个字", null, 400);
        }

        Integer requestLevel = requestReport.getLevel();
        if (requestLevel == null || requestLevel < 1 || requestLevel > 6) {
            return new ApiResponse<>("请选择真实的身体反馈", null, 400);
        }

        if (coverImg.isEmpty()) {
            return new ApiResponse<>("请上传一张实测图片", null, 400);
        }

        // 3. 找老祖宗 (查 Food 表)
        Food food = foodMapper.selectByBrandAndName(brandName, foodName);
        if (food == null) {
            // 没找到，说明是全网首发！新建一个
            food = new Food();
            food.setBrandName(brandName);
            food.setFoodName(foodName);
            food.setCoverImg(coverImg);
            foodMapper.insert(food);
            // 👆 因为加了 @Options，执行完这句，food.getId() 就有值了！
        } else if (!coverImg.isEmpty()) {
            food.setCoverImg(coverImg);
        }

        // 4. 记流水账 (插入 Report)
        requestReport.setFoodId(food.getId());
        requestReport.setUserId(currentUserId);
        // 🌟 把前端传过来的 level 赋值给数据库真正需要的 reactionLevel 字段
        requestReport.setReactionLevel(requestLevel);
        requestReport.setImagesJson(coverImg);

        dietReportMapper.insert(requestReport);
        // 👆 (注意：Mapper 的 @Insert SQL 里千万别写 brand 和 product 这俩假字段，完美骗过数据库)

        // 5. 重新计算大盘数据 (更新 Food)
        food.setTotalVotes(safeInt(food.getTotalVotes()) + 1);

        // 根据用户的选择，给对应的等级票数 +1
        int level = requestReport.getReactionLevel();
        if (level == 1) food.setLevel1Votes(safeInt(food.getLevel1Votes()) + 1);
        else if (level == 2) food.setLevel2Votes(safeInt(food.getLevel2Votes()) + 1);
        else if (level == 3) food.setLevel3Votes(safeInt(food.getLevel3Votes()) + 1);
        else if (level == 4) food.setLevel4Votes(safeInt(food.getLevel4Votes()) + 1);
        else if (level == 5) food.setLevel5Votes(safeInt(food.getLevel5Votes()) + 1);
        else if (level == 6) food.setLevel6Votes(safeInt(food.getLevel6Votes()) + 1);

        // 6. 计算核心指标：安全率 = (绿区 + 黄区) / 总人数 * 100
        int safeCount = safeInt(food.getLevel1Votes()) + safeInt(food.getLevel2Votes());
        int safeRate = (int) Math.round(((double) safeCount / food.getTotalVotes()) * 100);
        food.setSafeRate(safeRate);

        // 7. 更新大盘
        foodMapper.updateStats(food);

        return new ApiResponse<>("战报已收录", food, 200);
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

    @GetMapping("/my-reports")
    public ApiResponse<List<DietReport>> getMyReports(@AuthenticationPrincipal LoginUser loginUser) {
        return new ApiResponse<>("获取成功", dietReportMapper.getReportsByUserId(loginUser.getUserId()), 200);
    }

    @PostMapping("/reports/{reportId}/update")
    @Transactional
    public ApiResponse<DietReport> updateReport(
            @PathVariable("reportId") Long reportId,
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody DietReport requestReport
    ) {
        DietReport oldReport = dietReportMapper.findById(reportId);
        if (oldReport == null) {
            return new ApiResponse<>("这条战报不存在", null, 404);
        }
        if (!oldReport.getUserId().equals(loginUser.getUserId())) {
            return new ApiResponse<>("只能修改自己的战报", null, 403);
        }

        Integer requestLevel = requestReport.getLevel() == null ? requestReport.getReactionLevel() : requestReport.getLevel();
        if (requestLevel == null || requestLevel < 1 || requestLevel > 6) {
            return new ApiResponse<>("请选择真实的身体反馈", null, 400);
        }

        String coverImg = requestReport.getCoverImg() == null ? "" : requestReport.getCoverImg().trim();
        if (coverImg.isEmpty()) {
            coverImg = requestReport.getImagesJson() == null ? "" : requestReport.getImagesJson().trim();
        }
        if (coverImg.isEmpty()) {
            return new ApiResponse<>("请上传一张实测图片", null, 400);
        }

        DietReport update = new DietReport();
        update.setId(reportId);
        update.setUserId(loginUser.getUserId());
        update.setReactionLevel(requestLevel);
        update.setLocation(cleanText(requestReport.getLocation()));
        update.setContent(cleanText(requestReport.getContent()));
        update.setImagesJson(coverImg);

        int updated = dietReportMapper.updateByOwner(update);
        if (updated <= 0) {
            return new ApiResponse<>("修改失败，稍后再试", null, 400);
        }

        foodMapper.updateCover(oldReport.getFoodId(), coverImg);
        foodMapper.refreshStats(oldReport.getFoodId());
        return new ApiResponse<>("战报已更新", dietReportMapper.findById(reportId), 200);
    }

    @PostMapping("/reports/{reportId}/delete")
    @Transactional
    public ApiResponse<String> deleteReport(
            @PathVariable("reportId") Long reportId,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        DietReport oldReport = dietReportMapper.findById(reportId);
        if (oldReport == null) {
            return new ApiResponse<>("这条战报不存在", null, 404);
        }
        if (!oldReport.getUserId().equals(loginUser.getUserId())) {
            return new ApiResponse<>("只能删除自己的战报", null, 403);
        }

        int deleted = dietReportMapper.softDeleteByOwner(reportId, loginUser.getUserId());
        if (deleted <= 0) {
            return new ApiResponse<>("删除失败，稍后再试", null, 400);
        }
        foodMapper.refreshStats(oldReport.getFoodId());
        return new ApiResponse<>("战报已删除", null, 200);
    }

    @GetMapping("/reports/{reportId}/comments")
    public ApiResponse<List<DietReportComment>> getReportComments(@PathVariable("reportId") Long reportId) {
        return new ApiResponse<>("获取成功", dietReportMapper.getCommentsByReportId(reportId), 200);
    }

    @PostMapping("/reports/{reportId}/comments")
    public ApiResponse<DietReportComment> commentReport(
            @PathVariable("reportId") Long reportId,
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody DietReportComment comment
    ) {
        DietReport report = dietReportMapper.findById(reportId);
        if (report == null) {
            return new ApiResponse<>("这条战报不存在", null, 404);
        }

        String content = cleanText(comment.getContent());
        if (content.isEmpty()) {
            return new ApiResponse<>("写点评论再发送", null, 400);
        }

        comment.setReportId(reportId);
        comment.setUserId(loginUser.getUserId());
        comment.setContent(content);
        dietReportMapper.insertComment(comment);
        return new ApiResponse<>("评论已发布", comment, 200);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String cleanText(String value) {
        return value == null ? "" : value.trim();
    }
}
