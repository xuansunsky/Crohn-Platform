package com.xuan.croprogram.controller;

import com.xuan.croprogram.config.JwtUtil;
import com.xuan.croprogram.mapper.UserMapper;
import com.xuan.croprogram.model.ApiResponse;
import com.xuan.croprogram.model.LoginUser;
import com.xuan.croprogram.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtil jwtUtil; // ✅ 注入JWT工具类
    // 如果不想要 Bean 配置文件，直接在这里硬核初始化
    @Autowired
    private PasswordEncoder passwordEncoder;
    // 用户注册
    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody User user) {
        // 1. 查重：看看是不是老面孔
        User existingUser = userMapper.findByPhoneNumber(user.getPhoneNumber());
        if (existingUser != null) {
            // 模仿 login 的回执风格：提示语、返回数据(null)、状态码(400)
            return new ApiResponse<>("这手机号已经有人占了，换一个吧！", null, 400);
        }

        // 2. 加密密码
        // ⚠️ 兄弟注意：如果删了 Bean，这里执行时会报错，咱们下文细说
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 3. 初始身份：默认都是 2 (平民 USER)
        user.setRoleId(2L);
// 如果没有上传头像，就给他随机生成一个
        if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
            // 使用 DiceBear API，根据用户名生成唯一头像
            String randomAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=" + user.getNickname();
            user.setAvatar(randomAvatar);
        }
        // 4. 写入名册
        userMapper.insertUser(user);

        // 成功回执：提示语、返回数据(null)、状态码(200)
        return new ApiResponse<>("欢迎加入 Kingdom！请开始你的表演。", null, 200);
    }

    // 用户登录
    @PostMapping("/login")
    public ApiResponse<Map<String,Object>> login(@RequestBody User loginReq) {
        // 1. 先去库里把这个人找出来
        User dbUser = userMapper.findByPhoneNumber(loginReq.getPhoneNumber());

        // 2. 开始比对：人得在，且密码得对
        // passwordEncoder.matches(前端传的明文, 数据库里的密文)
        if (dbUser != null && passwordEncoder.matches(loginReq.getPassword(), dbUser.getPassword())) {

            // 3. ✅ 成功！生成工牌 (带上 roleId)
            String token = jwtUtil.generateToken(dbUser.getUserId(),dbUser.getPhoneNumber(), dbUser.getRoleId(),dbUser.getNickname());
            Map<String, Object> loginData = new HashMap<>();
            loginData.put("token", token);
            loginData.put("roleId", dbUser.getRoleId());
            loginData.put("userId", dbUser.getUserId());


            return new ApiResponse<>("登录成功！！", loginData, 200);


        } else {
            return new ApiResponse<>("账号或密码不对，兄弟你再想想？", null, 401);
        }
    }
    // 🔍 获取名册 (仅限国王阅览)
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')") // 🌟 这一行，顶替了之前所有的 if (roleId != 1)
    public ApiResponse<List<User>> getAllUsers() {
        // 既然能进来，你就是国王！不需要传 Token，不需要 substring(7)1

        List<User> users = userMapper.findAllUsers();
        return new ApiResponse<>("名册在此，请国王阅览。", users, 200);
    }

    // ⚒️ 敕封/贬职接口 (仅限国王授勋)
    @PostMapping("/updateRole")
    @PreAuthorize("hasRole('ADMIN')") // 🌟 这一行，直接把非法请求拦在门外
    public ApiResponse<String> updateRole(@RequestBody Map<String, Long> params) {
        // 连 Header 参数都不用传了，Spring 会自动处理认证
        Long userId = params.get("userId");
        Long targetRoleId = params.get("roleId");

        userMapper.updateRole(userId, targetRoleId);
        return new ApiResponse<>("King的旨意已下达，身份已变更。", null, 200);
    }

    @GetMapping("/getRole") // 建议加个路径，显得清晰，比如 /user/getRole
    public ApiResponse<Long> getRole(@AuthenticationPrincipal LoginUser loginUser) {

        // 1. 🔒 获取当前登录用户的 ID (这是最关键的一步)
        // 这里的 SecurityUtils.getUserId() 是你项目里封装的工具类
        // 如果没有封装，通常是 SecurityContextHolder.getContext().getAuthentication()... 拿出来的
        Long currentUserId = loginUser.getUserId();
        System.out.println(loginUser);
        // 2. 🔍 去数据库查验真身
        // 如果查不到（比如用户被删了），MyBatis 可能会返回 null，这里要做好心理准备
        Long realRoleId = userMapper.selectRoleIdByUserId(currentUserId);

        // 3. 🛡️ 防御性编程：万一查出来是 null (比如数据异常)，默认给(平民)
        if (realRoleId == null) {
            realRoleId = 2L;
        }

        // 4. 🚀 返回给前端
        return new ApiResponse<>("身份核验通过，当前权限已同步。", realRoleId, 200);
    }

}
