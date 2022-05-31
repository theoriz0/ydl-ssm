package com.ydlclass.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ydlclass.entity.YdlLoginUser;
import com.ydlclass.entity.YdlUser;
import com.ydlclass.service.YdlUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Slf4j
public class IndexController {
    @Resource
    private YdlUserService userService;

    @PostMapping("login")
    public ResponseEntity<YdlLoginUser> login(@RequestBody @Validated YdlUser ydlUser, BindingResult bindingResult) {
        //1. 校验
        List<ObjectError> allErrors = bindingResult.getAllErrors();
        allErrors.forEach(error -> log.error("登陆时用户校验失败: {}", error.getDefaultMessage()));
        if (allErrors.size() > 0) {
            return ResponseEntity.status(500).build();
        }
        //2. 登陆
        YdlLoginUser ydlLoginUser = null;
        try {
            ydlLoginUser = userService.login(ydlUser.getUserName(), ydlUser.getPassword());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok().body(ydlLoginUser);
    }
}
