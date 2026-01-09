package com.xuan.croprogram.controller;

import com.xuan.croprogram.model.*;
import com.xuan.croprogram.mapper.FriendshipMapper;
import com.xuan.croprogram.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friend")
public class FriendController {

    @Autowired
    private FriendshipMapper friendMapper;

    @Autowired
    private UserMapper userMapper; // 用来检查查无此人

    /**
     * 1. 发送好友申请
     * URL: POST /api/friend/request
     * Body: { "friendId": 102 }  <-- 这里的 friendId 其实是借用 Friendship 里的 addresseeId 或者是你自定义的字段，
     * 为了方便，建议前端传参时，后端用 Friendship 对象接，前端要把对方ID放在 friendId 字段里(如果DTO有) 或者直接放在 addresseeId
     * * 修正：为了最简单，我们这里用 FriendDto 或者直接 Map 接，或者直接用 Friendship 实体接
     * 咱们约定：前端传 { "addresseeId": 102 }
     */
    @PostMapping("/request")
    public ApiResponse<String> sendRequest(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody Friendship request // 前端传 { "addresseeId": 对方ID }
    ) {
        Long myId = loginUser.getId();
        Long targetId = request.getAddresseeId(); // 注意：前端 JSON key 必须叫 addresseeId

        // === 第一关：不能自恋 ===
        if (myId.equals(targetId)) {
            return new ApiResponse<>("不能添加自己为好友", null, 400);
        }

        // === 第二关：检查对方是否存在 ===
        // 如果你 UserMapper 里有 findById 就打开下面这两行，没有就算了
        // User target = userMapper.findById(targetId);
        // if (target == null) return new ApiResponse<>("用户不存在", null, 404);

        // === 第三关：是不是已经有关系了？ ===
        Friendship existing = friendMapper.findRelation(myId, targetId);
        if (existing != null) {
            if ("ACCEPTED".equals(existing.getStatus())) {
                return new ApiResponse<>("你们已经是好友了", null, 400);
            }
            if ("PENDING".equals(existing.getStatus())) {
                return new ApiResponse<>("申请已发送，请耐心等待", null, 400);
            }
        }

        // === 第四关：入库 ===
        Friendship newShip = new Friendship();
        newShip.setRequesterId(myId);  // 发起人是我
        newShip.setAddresseeId(targetId); // 接收人是他
        newShip.setStatus("PENDING"); // 状态：等待通过

        friendMapper.insert(newShip);

        return new ApiResponse<>("好友申请已发送！", null, 200);
    }

    /**
     * 2. 获取我的好友列表
     * URL: GET /api/friend/list
     */
    @GetMapping("/list")
    public ApiResponse<List<FriendDto>> getFriendList(@AuthenticationPrincipal LoginUser loginUser) {
        // 调用 Mapper 那个复杂的 SQL
        List<FriendDto> list = friendMapper.findMyFriends(loginUser.getId());
        return new ApiResponse<>("获取成功", list, 200);
    }

    /**
     * 3. 获取谁想加我 (申请列表)
     * URL: GET /api/friend/requests
     */
    @GetMapping("/requests")
    public ApiResponse<List<FriendDto>> getPendingRequests(@AuthenticationPrincipal LoginUser loginUser) {
        List<FriendDto> list = friendMapper.findPendingRequests(loginUser.getId());
        return new ApiResponse<>("获取成功", list, 200);
    }

    /**
     * 4. 同意好友申请
     * URL: POST /api/friend/accept/{id}  <-- 这里的 id 是 friendship 表的主键ID
     */
    @PostMapping("/accept/{id}")
    public ApiResponse<String> acceptRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        // 先查出来看看是谁申请的
        Friendship request = friendMapper.findById(id);

        if (request == null) {
            return new ApiResponse<>("申请记录不存在", null, 404);
        }

        // 改状态
        friendMapper.updateStatus(id, "ACCEPTED");

        return new ApiResponse<>("已同意！", null, 200);
    }
}