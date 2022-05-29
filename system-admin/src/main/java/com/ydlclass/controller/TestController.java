package com.ydlclass.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController{
    @GetMapping("user")
    @ResponseBody
    public User test() {
        return new User("tom", 12);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    static class User {
        String name;
        Integer age;
    }
}
