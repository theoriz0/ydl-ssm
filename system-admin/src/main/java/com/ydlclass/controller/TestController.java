package com.ydlclass.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ydlclass.configuration.RedisTemplate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;


@Controller
@Slf4j
public class TestController{

    @Resource
    private RedisTemplate redisTemplate;

    @GetMapping("test")
    @ResponseBody
    public String test() {
        redisTemplate.setObject("map", List.of("zs", "lisi", "ww"), -1L);
        List<String> list = redisTemplate.getObject("map", new TypeReference<>(){});
        log.info(list.toString());
        return "hello-ssm";
    }

    @Getter
    @Setter
    @AllArgsConstructor
    static class User {
        String name;
        Integer age;
    }
}
