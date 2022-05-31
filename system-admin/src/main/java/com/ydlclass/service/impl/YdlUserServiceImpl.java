package com.ydlclass.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.ydlclass.configuration.CustomObjectMapper;
import com.ydlclass.configuration.RedisTemplate;
import com.ydlclass.constant.Constants;
import com.ydlclass.entity.YdlLoginUser;
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
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

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
