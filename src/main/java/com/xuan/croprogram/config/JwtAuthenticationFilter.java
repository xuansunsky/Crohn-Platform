package com.xuan.croprogram.config;

import com.xuan.croprogram.model.LoginUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private  JwtUtil jwtUtil;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 🕵️ 第一关：看有没有带“工牌”
        // 从 HTTP 请求的抽屉（Header）里找那个叫 Authorization 的盒子
        final String authHeader = request.getHeader("Authorization");

        // 如果盒子是空的，或者里面装的不是以 "Bearer " 开头的暗号
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 直接放行！保安不拦你，但你现在是“无名氏”身份
            // 如果你去的是注册/登录，大门会让你过；如果你去的是名册，后面会有别的关卡拦住你
            filterChain.doFilter(request, response);
            return;
        }

        // 🔍 第二关：拆开“工牌”，读取信息
        try {
            // 撕掉 "Bearer " 这层包装纸，拿到真正的 JWT 乱码
            String jwt = authHeader.substring(7);

            // 用咱们的工具类解析出：他是谁（手机号）？他是啥职位（roleId）？
            String phoneNumber = jwtUtil.extractUsername(jwt);
            Long roleId = jwtUtil.getRoleIdFromToken(jwt);
            Long id = jwtUtil.getUserIdFromToken(jwt);
            // 📝 第三关：做一张官方认可的“临时身份证”
            // 如果解析出了名字，且目前系统还没给他登记过（SecurityContext 为空）
            if (phoneNumber != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 验证一下工牌有没有过期，秘钥对不对
                if (jwtUtil.validateToken(jwt, phoneNumber)) {
                    LoginUser loginUser = new LoginUser(id, phoneNumber, roleId);
                    // 🌟 这里是重点！咱们把数字 roleId 翻译成 Spring 认得的“职位等级”
                    // 1 -> ROLE_ADMIN, 2 -> ROLE_USER (必须以 ROLE_ 开头，这是 Spring 的规矩)
                    String roleName = (roleId != null && roleId == 1) ? "ROLE_ADMIN" : "ROLE_USER";

                    // 把它包装成一个权限对象
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(roleName));

                    // 创建官方“认证令牌”：包含用户名、密码(不要了给null)、权限等级
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(loginUser, null, authorities);

                    // 顺便记录一下他从哪台电脑发出的请求
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 📥 第四关：把“身份证”塞进王国的共享口袋
                    // 这一步之后，后面的 Controller 就能通过 @PreAuthorize 直接认出他是国王还是平民
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // 如果解析过程中炸了（比如有人伪造工牌），保安就当没看见，不给他登记，让他以“无名氏”身份往后走
            System.out.println("安保提示：工牌有问题，暂不予认证。原因：" + e.getMessage());
        }

        // 🚀 最后一关：大喊一声“下一个！”，让请求继续往后走
        filterChain.doFilter(request, response);
    }
}
