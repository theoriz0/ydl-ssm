package com.ydlclass.interceptor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ydlclass.configuration.RedisTemplate;
import com.ydlclass.constant.Constants;
import com.ydlclass.entity.YdlLoginUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class LoginInterceptor implements HandlerInterceptor {
    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        //1. 设置一个白名单（通过xml配置，故注释掉）
//        List<String> whiteNames = List.of("/login");
//        if (whiteNames.contains(request.getRequestURI())) {
//            return true;
//        }

        //2. 如果不是白名单，检测
        //判断Header有没有Authorization, 如果有，读取首部信息
        String token = request.getHeader(Constants.HEADER_AUTH);
        if (token == null) {
            ResponseEntity<String> res = ResponseEntity.status(401).body("Unauthorized");
            response.setStatus(401);
            response.getWriter().write(objectMapper.writeValueAsString(res));
            return false;
        }
        YdlLoginUser loginUser = redisTemplate.getObject(Constants.TOKEN_PREFIX + token, new TypeReference<YdlLoginUser>() {});
        if (loginUser == null) {
            ResponseEntity<String> res = ResponseEntity.status(401).body("Bad Credentials");
            response.setStatus(401);
            response.getWriter().write(objectMapper.writeValueAsString(res));
            return false;
        }
        //3. 使用token去redis，看有没有对应的loginUser
        return true;
    }
}
