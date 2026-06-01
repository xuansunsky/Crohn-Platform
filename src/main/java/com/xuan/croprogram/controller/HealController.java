package com.xuan.croprogram.controller;

import com.xuan.croprogram.mapper.HealMapper;
import com.xuan.croprogram.model.ApiResponse;
import com.xuan.croprogram.model.LoginUser;
import com.xuan.croprogram.model.PainSignal;
import com.xuan.croprogram.model.PaperBoat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/heal")
public class HealController {

    @Autowired
    private HealMapper healMapper;

    /**
     * 获取用户当前的暖心值
     */
    @GetMapping("/warmth")
    public ApiResponse<Integer> getWarmth(@AuthenticationPrincipal LoginUser loginUser) {
        Long userId = loginUser.getUserId();
        Integer points = healMapper.findWarmthPoints(userId);
        if (points == null) {
            points = 36;
            healMapper.saveWarmthPoints(userId, points);
        }
        return new ApiResponse<>("查询成功", points, 200);
    }

    /**
     * 发起痛痛广播呼救
     */
    @PostMapping("/pain/broadcast")
    public ApiResponse<String> broadcastPain(@AuthenticationPrincipal LoginUser loginUser, @RequestBody Map<String, String> req) {
        Long userId = loginUser.getUserId();
        String location = req.getOrDefault("location", "神秘星系");
        
        // 1. 先关闭之前的活跃信号（防止刷屏）
        healMapper.deactivatePainSignal(userId);
        
        // 2. 发起新呼救
        PainSignal signal = new PainSignal();
        signal.setUserId(userId);
        signal.setLocation(location);
        healMapper.insertPainSignal(signal);
        
        return new ApiResponse<>("信号已发射 📡，有我们在！", null, 200);
    }

    /**
     * 获取当前附近战友的活跃痛苦共鸣信号
     */
    @GetMapping("/pain/patrol")
    public ApiResponse<List<PainSignal>> getActiveSignals(@AuthenticationPrincipal LoginUser loginUser) {
        Long userId = loginUser.getUserId();
        List<PainSignal> signals = healMapper.findActivePainSignals(userId);
        
        // 判断当前用户是否已经给对方揉过肚子送过温暖
        signals.forEach(s -> {
            s.setWarmed(healMapper.checkComforted(s.getId(), userId) > 0);
        });
        
        return new ApiResponse<>("拉取巡视列表成功", signals, 200);
    }

    /**
     * 为他人揉腹/送暖水袋（暖暖他人）
     */
    @PostMapping("/pain/comfort")
    public ApiResponse<String> comfortFriend(@AuthenticationPrincipal LoginUser loginUser, @RequestBody Map<String, Long> req) {
        Long signalId = req.get("signalId");
        Long userId = loginUser.getUserId();
        
        if (signalId == null) {
            return new ApiResponse<>("信号ID不能为空", null, 400);
        }
        
        // 插入安慰记录
        int inserted = healMapper.insertComfort(signalId, userId, "rub");
        if (inserted > 0) {
            // 给当前用户增加 15 暖心积分！
            healMapper.addWarmthPoints(userId, 15);
            return new ApiResponse<>("暖腹微光送达！暖心值 +15 🩹", null, 200);
        }
        return new ApiResponse<>("你已经给这位战友送过温暖啦 🫂", null, 200);
    }

    /**
     * 放飞解压纸船（粒子碎裂后调用，彻底解压）
     */
    @PostMapping("/paperboat/release")
    public ApiResponse<String> releasePaperBoat(@RequestBody Map<String, String> req) {
        String content = req.get("content");
        if (content == null || content.trim().isEmpty()) {
            return new ApiResponse<>("写点心事吧 ⛵", null, 400);
        }
        
        PaperBoat boat = new PaperBoat();
        boat.setContent(content);
        healMapper.insertPaperBoat(boat);
        
        return new ApiResponse<>("情绪已被黑洞吸收，负荷消逝 🌠", null, 200);
    }

    /**
     * 随机捞取一个漂流纸船
     */
    @GetMapping("/paperboat/scoop")
    public ApiResponse<PaperBoat> scoopPaperBoat() {
        PaperBoat boat = healMapper.findRandomPaperBoat();
        if (boat == null) {
            return new ApiResponse<>("小河清澈，暂时没有捞到心事 🌤️", null, 200);
        }
        return new ApiResponse<>("成功捞起匿名心事 ⛵", boat, 200);
    }

    /**
     * 对纸船吹拂微风送去默默鼓励
     */
    @PostMapping("/paperboat/breeze")
    public ApiResponse<String> blowBreeze(@AuthenticationPrincipal LoginUser loginUser, @RequestBody Map<String, Long> req) {
        Long boatId = req.get("boatId");
        Long userId = loginUser.getUserId();
        
        if (boatId == null) {
            return new ApiResponse<>("纸船ID不能为空", null, 400);
        }
        
        // 记录吹风互动
        int inserted = healMapper.insertBreeze(boatId, userId);
        if (inserted > 0) {
            // 增加纸船的受抚次数
            healMapper.incrementBreeze(boatId);
            // 给吹风者增加 10 暖心积分！
            healMapper.addWarmthPoints(userId, 10);
            return new ApiResponse<>("微风已吹向远方，暖心值 +10 🍃", null, 200);
        }
        return new ApiResponse<>("这只小纸船已经接收过你的微风啦 🍃", null, 200);
    }
}
