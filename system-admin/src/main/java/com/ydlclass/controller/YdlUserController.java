package com.ydlclass.controller;

import com.ydlclass.entity.YdlUser;
import com.ydlclass.service.YdlUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * 用户信息表(YdlUser)表控制层
 *
 * @author makejava
 * @since 2022-05-30 00:12:56
 */
@RestController
@RequestMapping("ydlUser")
public class YdlUserController {
    /**
     * 服务对象
     */
    @Resource
    private YdlUserService ydlUserService;

    /**
     * 分页查询
     *
     * @param ydlUser 筛选条件
     * @return 查询结果
     */
    @GetMapping
    public ResponseEntity<Page<YdlUser>> queryByPage(YdlUser ydlUser) {
        return ResponseEntity.ok(this.ydlUserService.queryByPage(ydlUser, PageRequest.of(ydlUser.getPage(), ydlUser.getSize())));
    }

    @GetMapping("getInfo")
    public ResponseEntity<HashMap<String, List<String>>> getInfo() {
        return ResponseEntity.ok(this.ydlUserService.getInfo());
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public ResponseEntity<YdlUser> queryById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(this.ydlUserService.queryById(id));
    }

    /**
     * 新增数据
     *
     * @param ydlUser 实体
     * @return 新增结果
     */
    @PostMapping
    public ResponseEntity<YdlUser> add(YdlUser ydlUser) {
        return ResponseEntity.ok(this.ydlUserService.insert(ydlUser));
    }

    /**
     * 编辑数据
     *
     * @param ydlUser 实体
     * @return 编辑结果
     */
    @PutMapping
    public ResponseEntity<YdlUser> edit(YdlUser ydlUser) {
        return ResponseEntity.ok(this.ydlUserService.update(ydlUser));
    }

    /**
     * 删除数据
     *
     * @param id 主键
     * @return 删除是否成功
     */
    @DeleteMapping
    public ResponseEntity<Boolean> deleteById(Long id) {
        return ResponseEntity.ok(this.ydlUserService.deleteById(id));
    }

}

