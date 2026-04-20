package com.xuan.croprogram.controller;

import com.xuan.croprogram.mapper.UserMapper;
import com.xuan.croprogram.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/radar")
public class RadarController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserMapper userMapper;

    // Redis 里存放雷达坐标的专属 Key
    private static final String RADAR_KEY = "crohn:radar:users";

    /**
     * 1. 开启雷达 (上报位置)
     * 前端传参: { "longitude": 105.05, "latitude": 29.58 }
     */
    @PostMapping("/join")
    public ApiResponse<String> joinRadar(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody RadarLocationReq req // 见下方注释
    ) {
        String myId = loginUser.getUserId().toString(); // Redis 里存 String 最好

        // 核心魔法：把经纬度和用户ID塞进 Redis 的地理位置池子里
        redisTemplate.opsForGeo().add(
                RADAR_KEY,
                new Point(req.getLongitude(), req.getLatitude()),
                myId
        );

        return new ApiResponse<>("已进入同城雷达池，坐标已锁定", null, 200);
    }

    /**
     * 2. 关闭雷达 (销毁位置)
     */
    @PostMapping("/leave")
    public ApiResponse<String> leaveRadar(@AuthenticationPrincipal LoginUser loginUser) {
        String myId = loginUser.getUserId().toString();

        // 核心魔法：把这个用户从池子里踢出去，保护隐私
        redisTemplate.opsForGeo().remove(RADAR_KEY, myId);

        return new ApiResponse<>("雷达已关闭，坐标已彻底销毁", null, 200);
    }

    /**
     * 3. 核心大招：雷达扫描！
     * 前端传参: ?radius=10 (代表扫方圆10公里，不传默认5公里)
     */
   /* @GetMapping("/scan")
    public ApiResponse<List<RadarUserDto>> scanRadar(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam(defaultValue = "5") double radius
    ) {
        String myId = loginUser.getId().toString();

        // 1. 核心魔法：告诉 Redis，以"我"为圆心，找半径 radius 公里内的人！
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo()
                .radius(RADAR_KEY, myId, new Distance(radius, Metrics.KILOMETERS));

        if (results == null || results.getContent().isEmpty()) {
            return new ApiResponse<>("方圆百里没有病友...", new ArrayList<>(), 200);
        }

        // 2. 解析 Redis 返回的粗糙数据 (提取出 ID 和 距离)
        List<RadarUserDto> radarUsers = new ArrayList<>();
        List<Long> userIds = new ArrayList<>();

        for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : results) {
            String targetIdStr = result.getContent().getName();
            if (targetIdStr.equals(myId)) continue; // 别把自己扫出来了！

            Long targetId = Long.parseLong(targetIdStr);
            userIds.add(targetId);

            // 格式化距离，比如把 3.2452 变成 "距你 3.2km"
            double distanceKm = result.getDistance().getValue();
            String distanceStr = String.format("距你 %.1fkm", distanceKm);

            // 先建一个半成品 DTO，把距离存起来
            RadarUserDto dto = new RadarUserDto();
            dto.setId(targetId);
            dto.setDistance(distanceStr);
            radarUsers.add(dto);
        }

        if (userIds.isEmpty()) {
            return new ApiResponse<>("周围没有其他病友", new ArrayList<>(), 200);
        }

        // 3. 去 MySQL 查户口：一次性把这些人的头像、网名查出来！
        List<User> userDetails = userMapper.findUsersByIds(userIds);

        // 4. 组装车间：把 MySQL 的资料，填补到刚才的半成品 DTO 里
        for (RadarUserDto dto : radarUsers) {
            for (User u : userDetails) {
                if (dto.getId().equals(u.getId())) {
                    dto.setName(u.getNickname());
                    dto.setAvatar(u.getAvatar());
                    dto.setSign(u.getSign());
                    dto.setTags(u.getTags()); // 假设你的数据库里存了破冰标签
                    break;
                }
            }
        }

        // 5. 大功告成，发给前端！
        return new ApiResponse<>("雷达扫描完毕！", radarUsers, 200);
    }*/
}

/* --- 补充的实体类 ---
class RadarLocationReq {
    private Double longitude;
    private Double latitude;
    // getters and setters...
}

class RadarUserDto {
    private Long id;
    private String name;
    private String avatar;
    private String distance; // "距你 3.2km"
    private String sign;
    private List<String> tags;
    // getters and setters...
}
*/