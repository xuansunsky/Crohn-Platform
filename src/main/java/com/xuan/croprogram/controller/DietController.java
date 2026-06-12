package com.xuan.croprogram.controller;


import com.xuan.croprogram.mapper.DietReportMapper;
import com.xuan.croprogram.mapper.FoodMapper;
import com.xuan.croprogram.model.ApiResponse;
import com.xuan.croprogram.model.DietFoodComment;
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
        }

        // 4. 一人一票：如果之前只投过票，这次发布就把那条记录补完整
        requestReport.setFoodId(food.getId());
        requestReport.setUserId(currentUserId);
        requestReport.setReactionLevel(requestLevel);
        requestReport.setLocation(cleanText(requestReport.getLocation()));
        requestReport.setContent(cleanText(requestReport.getContent()));
        requestReport.setImagesJson(coverImg);

        DietReport mine = dietReportMapper.findMineByFoodId(food.getId(), currentUserId);
        if (mine != null) {
            requestReport.setId(mine.getId());
            dietReportMapper.updateByOwner(requestReport);
            foodMapper.updateCover(food.getId(), coverImg);
            foodMapper.refreshStats(food.getId());
            return new ApiResponse<>("实测已更新", foodMapper.findById(food.getId()), 200);
        }

        dietReportMapper.insert(requestReport);
        foodMapper.updateCover(food.getId(), coverImg);
        foodMapper.refreshStats(food.getId());

        return new ApiResponse<>("实测已收录", food, 200);
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

    @PostMapping("/foods/{foodId}/vote")
    @Transactional
    public ApiResponse<DietReport> voteFood(
            @PathVariable("foodId") Long foodId,
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody DietReport requestReport
    ) {
        Food food = foodMapper.findById(foodId);
        if (food == null) {
            return new ApiResponse<>("这条情报不存在", null, 404);
        }

        Integer requestLevel = requestReport.getLevel() == null ? requestReport.getReactionLevel() : requestReport.getLevel();
        if (requestLevel == null || requestLevel < 1 || requestLevel > 6) {
            return new ApiResponse<>("请选择真实的身体反馈", null, 400);
        }

        DietReport mine = dietReportMapper.findMineByFoodId(foodId, loginUser.getUserId());
        if (mine != null) {
            DietReport update = new DietReport();
            update.setId(mine.getId());
            update.setUserId(loginUser.getUserId());
            update.setReactionLevel(requestLevel);
            update.setLocation(cleanText(mine.getLocation()));
            update.setContent(cleanText(mine.getContent()));
            update.setImagesJson(mine.getImagesJson());
            dietReportMapper.updateByOwner(update);
            foodMapper.refreshStats(foodId);
            return new ApiResponse<>("投票已更新", dietReportMapper.findById(mine.getId()), 200);
        }

        DietReport report = new DietReport();
        report.setFoodId(foodId);
        report.setUserId(loginUser.getUserId());
        report.setReactionLevel(requestLevel);
        report.setLocation("");
        report.setContent("");
        report.setImagesJson(food.getCoverImg());
        dietReportMapper.insert(report);
        foodMapper.refreshStats(foodId);
        return new ApiResponse<>("投票成功", report, 200);
    }

    @PostMapping("/foods/{foodId}/feedback")
    @Transactional
    public ApiResponse<DietReport> feedbackFood(
            @PathVariable("foodId") Long foodId,
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody DietReport requestReport
    ) {
        Food food = foodMapper.findById(foodId);
        if (food == null) {
            return new ApiResponse<>("这条情报不存在", null, 404);
        }

        Integer requestLevel = requestReport.getLevel() == null ? requestReport.getReactionLevel() : requestReport.getLevel();
        if (requestLevel == null || requestLevel < 1 || requestLevel > 6) {
            return new ApiResponse<>("请选择真实的身体反馈", null, 400);
        }

        String content = cleanText(requestReport.getContent());
        if (content.isEmpty()) {
            return new ApiResponse<>("写一句真实感受再发送", null, 400);
        }

        DietReport mine = dietReportMapper.findMineByFoodId(foodId, loginUser.getUserId());
        if (mine != null) {
            DietReport update = new DietReport();
            update.setId(mine.getId());
            update.setUserId(loginUser.getUserId());
            update.setReactionLevel(requestLevel);
            update.setLocation(cleanText(requestReport.getLocation()));
            update.setContent(content);
            update.setImagesJson(mine.getImagesJson());
            dietReportMapper.updateByOwner(update);
            foodMapper.refreshStats(foodId);
            return new ApiResponse<>("你的反馈已更新", dietReportMapper.findById(mine.getId()), 200);
        }

        DietReport report = new DietReport();
        report.setFoodId(foodId);
        report.setUserId(loginUser.getUserId());
        report.setReactionLevel(requestLevel);
        report.setLocation(cleanText(requestReport.getLocation()));
        report.setContent(content);
        report.setImagesJson(food.getCoverImg());
        dietReportMapper.insert(report);
        foodMapper.refreshStats(foodId);
        return new ApiResponse<>("你的反馈已收录", report, 200);
    }

    @GetMapping("/foods/{foodId}/comments")
    public ApiResponse<List<DietFoodComment>> getFoodComments(@PathVariable("foodId") Long foodId) {
        return new ApiResponse<>("获取成功", dietReportMapper.getFoodComments(foodId), 200);
    }

    @PostMapping("/foods/{foodId}/comments")
    public ApiResponse<DietFoodComment> commentFood(
            @PathVariable("foodId") Long foodId,
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody DietFoodComment comment
    ) {
        Food food = foodMapper.findById(foodId);
        if (food == null) {
            return new ApiResponse<>("这条情报不存在", null, 404);
        }

        String content = cleanText(comment.getContent());
        if (content.isEmpty()) {
            return new ApiResponse<>("写点评论再发送", null, 400);
        }

        comment.setFoodId(foodId);
        comment.setUserId(loginUser.getUserId());
        comment.setContent(content);
        dietReportMapper.insertFoodComment(comment);
        return new ApiResponse<>("评论已发布", comment, 200);
    }

    @PostMapping("/foods/{foodId}/comments/{commentId}/delete")
    public ApiResponse<String> deleteFoodComment(
            @PathVariable("foodId") Long foodId,
            @PathVariable("commentId") Long commentId,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        int deleted = dietReportMapper.softDeleteFoodCommentByOwner(foodId, commentId, loginUser.getUserId());
        if (deleted <= 0) {
            return new ApiResponse<>("只能删除自己的评论", null, 403);
        }
        return new ApiResponse<>("评论已删除", null, 200);
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
            return new ApiResponse<>("这条实测不存在", null, 404);
        }
        if (!oldReport.getUserId().equals(loginUser.getUserId())) {
            return new ApiResponse<>("只能修改自己的实测", null, 403);
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
        return new ApiResponse<>("实测已更新", dietReportMapper.findById(reportId), 200);
    }

    @PostMapping("/reports/{reportId}/delete")
    @Transactional
    public ApiResponse<String> deleteReport(
            @PathVariable("reportId") Long reportId,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        DietReport oldReport = dietReportMapper.findById(reportId);
        if (oldReport == null) {
            return new ApiResponse<>("这条实测不存在", null, 404);
        }
        if (!oldReport.getUserId().equals(loginUser.getUserId())) {
            return new ApiResponse<>("只能删除自己的实测", null, 403);
        }

        int deleted = dietReportMapper.softDeleteByOwner(reportId, loginUser.getUserId());
        if (deleted <= 0) {
            return new ApiResponse<>("删除失败，稍后再试", null, 400);
        }
        foodMapper.refreshStats(oldReport.getFoodId());
        return new ApiResponse<>("实测已删除", null, 200);
    }

    private String cleanText(String value) {
        return value == null ? "" : value.trim();
    }
}
