package com.ydlclass.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.ydlclass.configuration.CustomObjectMapper;
import com.ydlclass.configuration.RedisTemplate;
import com.ydlclass.constant.Constants;
import com.ydlclass.entity.YdlLoginUser;
import com.ydlclass.entity.YdlMenu;
import com.ydlclass.entity.YdlRole;
import com.ydlclass.entity.YdlUser;
import com.ydlclass.dao.YdlUserDao;
import com.ydlclass.exception.PasswordIncorrectException;
import com.ydlclass.exception.UserNotFoundException;
import com.ydlclass.service.YdlUserService;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户信息表(YdlUser)表服务实现类
 *
 * @author makejava
 * @since 2022-05-30 00:12:56
 */
@Service("ydlUserService")
@Slf4j
public class YdlUserServiceImpl implements YdlUserService {
    @Resource
    private YdlUserDao ydlUserDao;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private CustomObjectMapper objectMapper;

    /**
     * 通过ID查询单条数据
     *
     * @param userId 主键
     * @return 实例对象
     */
    @Override
    public YdlUser queryById(Long userId) {
        return this.ydlUserDao.queryById(userId);
    }

    /**
     * 分页查询
     *
     * @param ydlUser 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */
    @Override
    public Page<YdlUser> queryByPage(YdlUser ydlUser, PageRequest pageRequest) {
        long total = this.ydlUserDao.count(ydlUser);
        return new PageImpl<>(this.ydlUserDao.queryAllByLimit(ydlUser, pageRequest), pageRequest, total);
    }

    /**
     * 新增数据
     *
     * @param ydlUser 实例对象
     * @return 实例对象
     */
    @Override
    public YdlUser insert(YdlUser ydlUser) {
        this.ydlUserDao.insert(ydlUser);
        return ydlUser;
    }

    /**
     * 修改数据
     *
     * @param ydlUser 实例对象
     * @return 实例对象
     */
    @Override
    public YdlUser update(YdlUser ydlUser) {
        this.ydlUserDao.update(ydlUser);
        return this.queryById(ydlUser.getUserId());
    }

    /**
     * 通过主键删除数据
     *
     * @param userId 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(Long userId) {
        return this.ydlUserDao.deleteById(userId) > 0;
    }

    @Override
    public HashMap<String, List<String>> getInfo() {
        // 1、获取当前登陆的对象
        YdlLoginUser loginUser = getLoginUser();

        // 2、查询当前用户的角色和权限
        YdlUser info = ydlUserDao.getInfo(loginUser.getUserId());

        // 3、处理权限和角色的相关信息
        // (1) roles:token : [admin,xxx,yyy]   perms:token: [system:user:add,system:user:update]
        List<String> roleTags = info.getYdlRoles().stream().map(YdlRole::getRoleTag).collect(Collectors.toList());
        redisTemplate.setObject(Constants.ROLE_PREFIX + loginUser.getToken(),roleTags,Constants.TOKEN_EXPIRE_SECONDS);

        List<String> prems = new ArrayList<>();
        // [{roleName:cc,roleTag:xxx,perms:[{id,'xxx',perm:'system'},{id,'xxx',perm:'system'}]},{}]
        // [[{id,'xxx',perm:'system'},{id,'xxx',perm:'system'}],[{id,'xxx',perm:'system'},{id,'xxx',perm:'system'}]]
        // ['system','system:user:add']
        info.getYdlRoles()
                .stream()
                .map(YdlRole::getYdlMenus)
                .forEach(menus -> {
                    prems.addAll(menus.stream()
                            .map(YdlMenu::getPerms)
                            .collect(Collectors.toList()));
                });
        redisTemplate.setObject(Constants.PERM_PREFIX + loginUser.getToken(),prems,Constants.TOKEN_EXPIRE_SECONDS);

        // 整合数据
        HashMap<String,List<String>> data = new HashMap<>();
        data.put("roles",roleTags);
        data.put("perms",prems);

        return data;
    }

    // 获取当前登陆用户的方法
    private YdlLoginUser getLoginUser(){

        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        // 获取首部信息的token
        String token = request.getHeader(Constants.HEADER_AUTH);

        if (token == null) {
            throw new RuntimeException("当前用户未登录！");
        }
        YdlLoginUser ydlLoginUser = redisTemplate.getObject(Constants.TOKEN_PREFIX + token, new TypeReference<YdlLoginUser>() {});
        if (ydlLoginUser== null){
            throw new RuntimeException("当前用户未登录！");
        }
        // 3、使用token去redis中查看，有没有对应的loginUser
        return ydlLoginUser;
    }

    @Override
    public void logout() {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String token = request.getHeader(Constants.HEADER_AUTH);
        redisTemplate.remove(Constants.TOKEN_PREFIX + token);
    }

    @Override
    public YdlLoginUser login(String userName, String password) throws JsonProcessingException {
        //1. 登陆，使用用户名查询用户
        YdlUser user = ydlUserDao.queryByUserName(userName);
        if (user == null) {
            throw new UserNotFoundException("登陆操作时, [" + userName + "]用户名对应用户不存在");
        }
        //2. 查到，比较密码
        if (!password.equals(user.getPassword())) {
            log.info("登陆操作时, [{}]对应密码错误", userName);
            throw new PasswordIncorrectException("登陆操作时, [" + userName + "]对应密码错误");
        }
        //3. 验证成功
        //3.1 生成token
        String token = UUID.randomUUID().toString();
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        ResponseEntity<String> forEntity = restTemplate.getForEntity("https://whois.pconline.com.cn/ipJson.jsp?ip=" + request.getRemoteHost() + "&json=true", String.class);
        Map<String, String> map = objectMapper.readValue(forEntity.getBody(), new TypeReference<>(){});
        //3.2 封装YdlLoginUser，保存到redis
        YdlLoginUser loginUser = YdlLoginUser.builder()
                .userId(user.getUserId())
                .token(token)
                .ipaddr(request.getRemoteAddr())
                .loginTime(new Date())
                .os(userAgent.getOperatingSystem().getName())
                .browser(userAgent.getBrowser().getName())
                .loginLocation(map.get("addr") + map.get("pro") + map.get("city") + map.get("region"))
                .ydlUser(user)
                .build();
        redisTemplate.setObject(Constants.TOKEN_PREFIX + token, loginUser, 30 * 60L);
        return loginUser;
    }
}
