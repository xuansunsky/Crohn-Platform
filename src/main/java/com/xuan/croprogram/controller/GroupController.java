package com.xuan.croprogram.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuan.croprogram.mapper.GroupMapper;
import com.xuan.croprogram.mapper.GroupMessageMapper;
import com.xuan.croprogram.mapper.SquadActivityMapper;
import com.xuan.croprogram.mapper.SquadTaskMapper;
import com.xuan.croprogram.mapper.UserMapper;
import com.xuan.croprogram.model.*;
import com.xuan.croprogram.util.AvatarPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/group")
public class GroupController {

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private GroupMessageMapper groupMessageMapper;

    @Autowired
    private SquadTaskMapper squadTaskMapper;

    @Autowired
    private SquadActivityMapper squadActivityMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 建群：{ "name": "我的小队", "memberIds": [102, 103] }
     */
    @PostMapping("/create")
    public ApiResponse<ChatGroup> create(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody Map<String, Object> req
    ) {
        Long myId = loginUser.getUserId();
        String name = req.get("name") == null ? "" : req.get("name").toString().trim();
        if (name.isEmpty()) {
            return new ApiResponse<>("请给小队起个名字", null, 400);
        }

        ChatGroup group = new ChatGroup();
        group.setName(name);
        group.setOwnerId(myId);
        group.setAvatar(AvatarPool.pick("group-" + name));
        groupMapper.insertGroup(group);

        // 队长入队
        groupMapper.insertMember(group.getId(), myId, "OWNER");

        // 拉入选中成员
        Object memberIdsObj = req.get("memberIds");
        if (memberIdsObj instanceof List) {
            for (Object o : (List<?>) memberIdsObj) {
                if (o == null) continue;
                Long memberId = Long.parseLong(o.toString());
                if (!memberId.equals(myId)) {
                    groupMapper.insertMember(group.getId(), memberId, "MEMBER");
                }
            }
        }

        ChatGroup created = groupMapper.findById(group.getId());
        return new ApiResponse<>("小队已创建", created, 200);
    }

    /**
     * 我加入的所有群
     */
    @GetMapping("/list")
    public ApiResponse<List<ChatGroup>> list(@AuthenticationPrincipal LoginUser loginUser) {
        List<ChatGroup> groups = groupMapper.findMyGroups(loginUser.getUserId());
        return new ApiResponse<>("获取成功", groups, 200);
    }

    /**
     * 群详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long id
    ) {
        Long myId = loginUser.getUserId();
        if (groupMapper.isMember(id, myId) == 0) {
            return new ApiResponse<>("你不在这个小队里", null, 403);
        }
        ChatGroup group = groupMapper.findById(id);
        Map<String, Object> data = new HashMap<>();
        data.put("group", group);
        data.put("myRole", groupMapper.findMemberRole(id, myId));
        return new ApiResponse<>("获取成功", data, 200);
    }

    /**
     * 更新群资料：{ name, avatar, notice }
     */
    @PostMapping("/{id}/profile")
    public ApiResponse<ChatGroup> updateProfile(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long id,
            @RequestBody ChatGroup req
    ) {
        Long myId = loginUser.getUserId();
        ChatGroup existing = groupMapper.findById(id);
        if (existing == null) {
            return new ApiResponse<>("小队不存在", null, 404);
        }
        if (!existing.getOwnerId().equals(myId)) {
            return new ApiResponse<>("只有队长可以修改小队资料", null, 403);
        }

        String name = req.getName() == null ? "" : req.getName().trim();
        if (name.isEmpty()) {
            return new ApiResponse<>("小队名称不能为空", null, 400);
        }

        existing.setName(name);
        existing.setAvatar(req.getAvatar() == null || req.getAvatar().trim().isEmpty() ? existing.getAvatar() : req.getAvatar().trim());
        existing.setNotice(req.getNotice() == null ? "" : req.getNotice().trim());
        groupMapper.updateGroup(existing);
        return new ApiResponse<>("小队资料已更新", groupMapper.findById(id), 200);
    }

    /**
     * 群成员列表
     */
    @GetMapping("/{id}/members")
    public ApiResponse<List<GroupMember>> members(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long id
    ) {
        if (groupMapper.isMember(id, loginUser.getUserId()) == 0) {
            return new ApiResponse<>("你不在这个小队里", null, 403);
        }
        return new ApiResponse<>("获取成功", groupMapper.findMembers(id), 200);
    }

    /**
     * 邀请成员：{ "memberIds": [104, 105] }
     */
    @PostMapping("/{id}/invite")
    public ApiResponse<String> invite(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long id,
            @RequestBody Map<String, Object> req
    ) {
        if (groupMapper.isMember(id, loginUser.getUserId()) == 0) {
            return new ApiResponse<>("你不在这个小队里", null, 403);
        }
        Object memberIdsObj = req.get("memberIds");
        if (memberIdsObj instanceof List) {
            for (Object o : (List<?>) memberIdsObj) {
                if (o == null) continue;
                groupMapper.insertMember(id, Long.parseLong(o.toString()), "MEMBER");
            }
        }
        return new ApiResponse<>("已邀请加入", null, 200);
    }

    /**
     * 移除成员（队长权限）
     */
    @PostMapping("/{id}/remove")
    public ApiResponse<String> removeMember(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long id,
            @RequestBody Map<String, Object> req
    ) {
        Long myId = loginUser.getUserId();
        ChatGroup group = groupMapper.findById(id);
        if (group == null) {
            return new ApiResponse<>("小队不存在", null, 404);
        }
        if (!group.getOwnerId().equals(myId)) {
            return new ApiResponse<>("只有队长可以移除成员", null, 403);
        }
        Object memberIdObj = req.get("memberId");
        if (memberIdObj == null) {
            return new ApiResponse<>("成员ID不能为空", null, 400);
        }
        Long memberId = Long.parseLong(memberIdObj.toString());
        if (memberId.equals(myId)) {
            return new ApiResponse<>("队长不能移除自己，可以先转让队长后退出", null, 400);
        }
        groupMapper.deleteMember(id, memberId);
        return new ApiResponse<>("已移出小队", null, 200);
    }

    /**
     * 退出群聊
     */
    @PostMapping("/{id}/leave")
    public ApiResponse<String> leave(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long id
    ) {
        Long myId = loginUser.getUserId();
        ChatGroup group = groupMapper.findById(id);
        if (group == null) {
            return new ApiResponse<>("小队不存在", null, 404);
        }
        if (groupMapper.isMember(id, myId) == 0) {
            return new ApiResponse<>("你不在这个小队里", null, 403);
        }
        if (group.getOwnerId().equals(myId) && groupMapper.countMembers(id) > 1) {
            return new ApiResponse<>("队长暂不能直接退出，请先移交队长或解散小队", null, 400);
        }
        groupMapper.deleteMember(id, myId);
        if (groupMapper.countMembers(id) == 0) {
            groupMapper.deleteGroup(id);
        }
        return new ApiResponse<>("已退出小队", null, 200);
    }

    /**
     * 群聊历史
     */
    @GetMapping("/{id}/messages")
    public ApiResponse<List<GroupMessage>> messages(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long id
    ) {
        if (groupMapper.isMember(id, loginUser.getUserId()) == 0) {
            return new ApiResponse<>("你不在这个小队里", null, 403);
        }
        return new ApiResponse<>("获取成功", groupMessageMapper.findByGroup(id), 200);
    }

    /**
     * 发群消息：{ "content": "大家好", "type": "text" }
     */
    @PostMapping("/{id}/send")
    public ApiResponse<String> send(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long id,
            @RequestBody GroupMessage message
    ) {
        Long myId = loginUser.getUserId();
        if (groupMapper.isMember(id, myId) == 0) {
            return new ApiResponse<>("你不在这个小队里", null, 403);
        }
        message.setGroupId(id);
        message.setSenderId(myId);
        if (message.getType() == null) message.setType("text");
        groupMessageMapper.insert(message);

        // 取发送者昵称头像
        User sender = userMapper.findByPhoneNumber(loginUser.getPhoneNumber());
        String senderName = sender == null ? "队友" : sender.getNickname();
        String senderAvatar = sender == null ? null : sender.getAvatar();

        // 构造 JSON 信封，推给除自己外的所有在线成员
        Map<String, Object> envelope = new HashMap<>();
        envelope.put("kind", "group");
        envelope.put("groupId", id);
        envelope.put("senderId", myId);
        envelope.put("senderName", senderName);
        envelope.put("senderAvatar", senderAvatar);
        envelope.put("content", message.getContent());
        envelope.put("type", message.getType());

        try {
            String payload = objectMapper.writeValueAsString(envelope);
            List<GroupMember> members = groupMapper.findMembers(id);
            for (GroupMember m : members) {
                if (!m.getUserId().equals(myId)) {
                    WebSocketServer.sendInfo(m.getUserId(), payload);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ApiResponse<>("发送成功", null, 200);
    }

    // ============ 小队任务看板 ============

    @GetMapping("/{id}/tasks")
    public ApiResponse<List<SquadTask>> tasks(@PathVariable Long id) {
        return new ApiResponse<>("获取成功", squadTaskMapper.findByGroup(id), 200);
    }

    /**
     * 新增任务：{ "label": "今日低渣早餐打卡", "priority": "日常" }
     */
    @PostMapping("/{id}/tasks")
    public ApiResponse<String> addTask(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long id,
            @RequestBody SquadTask task
    ) {
        Long myId = loginUser.getUserId();
        if (groupMapper.isMember(id, myId) == 0) {
            return new ApiResponse<>("你不在这个小队里", null, 403);
        }
        if (task.getLabel() == null || task.getLabel().trim().isEmpty()) {
            return new ApiResponse<>("任务内容不能为空", null, 400);
        }
        task.setGroupId(id);
        task.setOwnerId(myId);
        if (task.getPriority() == null) task.setPriority("日常");
        squadTaskMapper.insert(task);

        // 顺手记一条队友动态
        SquadActivity act = new SquadActivity();
        act.setGroupId(id);
        act.setUserId(myId);
        String actionMsg = "新建了任务「" + task.getLabel() + "」";
        if (task.getAssigneeId() != null) {
            User assignee = userMapper.findByUserId(task.getAssigneeId());
            String assigneeName = assignee == null ? "队友" : assignee.getNickname();
            actionMsg += "，指派给 " + assigneeName;
        }
        act.setAction(actionMsg);
        squadActivityMapper.insert(act);

        return new ApiResponse<>("任务已添加", null, 200);
    }

    /**
     * 勾选/取消任务
     */
    @PostMapping("/tasks/{taskId}/toggle")
    public ApiResponse<String> toggleTask(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long taskId
    ) {
        SquadTask task = squadTaskMapper.findById(taskId);
        if (task == null) {
            return new ApiResponse<>("任务不存在", null, 404);
        }
        int newDone = (task.getDone() != null && task.getDone() == 1) ? 0 : 1;
        squadTaskMapper.updateDone(taskId, newDone);

        if (newDone == 1) {
            SquadActivity act = new SquadActivity();
            act.setGroupId(task.getGroupId());
            act.setUserId(loginUser.getUserId());
            act.setAction("完成了任务「" + task.getLabel() + "」✓");
            squadActivityMapper.insert(act);
        }

        return new ApiResponse<>("已更新", null, 200);
    }

    /**
     * 我来认领任务（把 assignee 设成自己）
     */
    @PostMapping("/tasks/{taskId}/claim")
    public ApiResponse<String> claimTask(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long taskId
    ) {
        Long myId = loginUser.getUserId();
        SquadTask task = squadTaskMapper.findById(taskId);
        if (task == null) {
            return new ApiResponse<>("任务不存在", null, 404);
        }
        if (groupMapper.isMember(task.getGroupId(), myId) == 0) {
            return new ApiResponse<>("你不在这个小队里", null, 403);
        }
        squadTaskMapper.updateAssignee(taskId, myId);

        User me = userMapper.findByUserId(myId);
        String myName = me == null ? "队友" : me.getNickname();
        SquadActivity act = new SquadActivity();
        act.setGroupId(task.getGroupId());
        act.setUserId(myId);
        act.setAction("认领了任务「" + task.getLabel() + "」");
        squadActivityMapper.insert(act);

        return new ApiResponse<>("认领成功", null, 200);
    }

    /**
     * 改派任务给某人：{ "assigneeId": 103 } ，传 null 则退回「待认领」
     */
    @PostMapping("/tasks/{taskId}/assign")
    public ApiResponse<String> assignTask(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long taskId,
            @RequestBody Map<String, Object> req
    ) {
        Long myId = loginUser.getUserId();
        SquadTask task = squadTaskMapper.findById(taskId);
        if (task == null) {
            return new ApiResponse<>("任务不存在", null, 404);
        }
        if (groupMapper.isMember(task.getGroupId(), myId) == 0) {
            return new ApiResponse<>("你不在这个小队里", null, 403);
        }
        Object aid = req.get("assigneeId");
        Long assigneeId = (aid == null) ? null : Long.parseLong(aid.toString());
        squadTaskMapper.updateAssignee(taskId, assigneeId);

        String actionMsg;
        if (assigneeId == null) {
            actionMsg = "把任务「" + task.getLabel() + "」退回了待认领池";
        } else {
            User assignee = userMapper.findByUserId(assigneeId);
            String name = assignee == null ? "队友" : assignee.getNickname();
            actionMsg = "把任务「" + task.getLabel() + "」指派给了 " + name;
        }
        SquadActivity act = new SquadActivity();
        act.setGroupId(task.getGroupId());
        act.setUserId(myId);
        act.setAction(actionMsg);
        squadActivityMapper.insert(act);

        return new ApiResponse<>("已更新指派", null, 200);
    }

    @GetMapping("/{id}/activities")
    public ApiResponse<List<SquadActivity>> activities(@PathVariable Long id) {
        return new ApiResponse<>("获取成功", squadActivityMapper.findByGroup(id), 200);
    }
}
