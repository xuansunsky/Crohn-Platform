package com.xuan.croprogram.controller;

import com.xuan.croprogram.mapper.DrugMapper;
import com.xuan.croprogram.model.ApiResponse;
import com.xuan.croprogram.model.Drug;
import com.xuan.croprogram.model.LoginUser;
import com.xuan.croprogram.model.UserCabinet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drug")
public class DrugController {

    @Autowired
    private DrugMapper drugMapper;

    @GetMapping("/list")
    public ApiResponse<List<Drug>> list() {
        return new ApiResponse<>("获取成功", drugMapper.findAll(), 200);
    }

    @GetMapping("/cabinet")
    public ApiResponse<List<UserCabinet>> getCabinet(@AuthenticationPrincipal LoginUser loginUser) {
        return new ApiResponse<>("获取成功", drugMapper.findCabinetByUserId(loginUser.getUserId()), 200);
    }

    @PostMapping("/cabinet/add")
    public ApiResponse<String> addToCabinet(@AuthenticationPrincipal LoginUser loginUser, @RequestBody UserCabinet cabinet) {
        cabinet.setUserId(loginUser.getUserId());
        drugMapper.insertCabinet(cabinet);
        return new ApiResponse<>("已加入药箱", null, 200);
    }

    @DeleteMapping("/cabinet/delete/{id}")
    public ApiResponse<String> deleteFromCabinet(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        drugMapper.deleteCabinet(id, loginUser.getUserId());
        return new ApiResponse<>("已移出药箱", null, 200);
    }
}
