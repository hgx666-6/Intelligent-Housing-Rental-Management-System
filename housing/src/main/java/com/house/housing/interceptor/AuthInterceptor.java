package com.house.housing.interceptor;

import com.house.housing.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    // 无需认证的路径
    private static final String[] EXCLUDE_PATHS = {
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/api/v1/houses/cities",
            "/api/v1/houses/types",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/doc.html"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        log.info("收到请求：{}",path);

        // 检查是否排除路径
        for (String excludePath : EXCLUDE_PATHS) {
            if (path.matches(excludePath.replace("**", ".*"))) {
                return true;
            }
        }

        // 获取 Token
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            try {
                response.getWriter().write("{\"code\":401,\"message\":\"未登录\"}");
            } catch (Exception e) {
                // ignore
            }
            return false;
        }
        String token = authorization.substring(7);

        // 验证 Token
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            try {
                response.getWriter().write("{\"code\":401,\"message\":\"Token无效或已过期\"}");
            } catch (Exception e) {
                // ignore
            }
            return false;
        }

        // 解析用户信息存入请求上下文
        Integer userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        Integer role = jwtUtil.getRoleFromToken(token);

        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        request.setAttribute("role", role);

        return true;
    }
}