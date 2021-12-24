package cn.iocoder.yudao.coreservice.modules.system.service.auth;

import cn.iocoder.yudao.coreservice.BaseDbAndRedisUnitTest;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.auth.SysUserSessionDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.mysql.auth.SysUserSessionCoreMapper;
import cn.iocoder.yudao.coreservice.modules.system.dal.redis.auth.SysLoginUserCoreRedisDAO;
import cn.iocoder.yudao.coreservice.modules.system.service.auth.impl.SysUserSessionCoreServiceImpl;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.security.config.SecurityProperties;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Date;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.addTime;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@Import({SysUserSessionCoreServiceImpl.class, SysLoginUserCoreRedisDAO.class})
public class SysUserSessionCoreServiceTest extends BaseDbAndRedisUnitTest {

    @Resource
    private SysUserSessionCoreServiceImpl userSessionCoreService;

    @Resource
    private SysUserSessionCoreMapper userSessionCoreMapper;
    @Resource
    private SysLoginUserCoreRedisDAO loginUserCoreRedisDAO;

    @MockBean
    private SecurityProperties securityProperties;

    @Test
    public void testCreateUserSession_success() {
        // 准备参数
        String userIp = randomString();
        String userAgent = randomString();
        LoginUser loginUser = randomPojo(LoginUser.class, o -> {
            o.setUserType(randomEle(UserTypeEnum.values()).getValue());
            o.setTenantId(0L); // 租户设置为 0，因为暂未启用多租户组件
        });
        // mock 方法
        when(securityProperties.getSessionTimeout()).thenReturn(Duration.ofDays(1));

        // 调用
        String sessionId = userSessionCoreService.createUserSession(loginUser, userIp, userAgent);
        // 校验 SysUserSessionDO 记录
        SysUserSessionDO userSessionDO = userSessionCoreMapper.selectById(sessionId);
        assertPojoEquals(loginUser, userSessionDO, "id", "updateTime");
        assertEquals(sessionId, userSessionDO.getId());
        assertEquals(userIp, userSessionDO.getUserIp());
        assertEquals(userAgent, userSessionDO.getUserAgent());
        // 校验 LoginUser 缓存
        LoginUser redisLoginUser = loginUserCoreRedisDAO.get(sessionId);
        assertPojoEquals(loginUser, redisLoginUser, "username", "password");
    }

    @Test
    public void testCreateRefreshUserSession_success() {
        // 准备参数
        String sessionId = randomString();
        String userIp = randomString();
        String userAgent = randomString();
        long timeLong = randomLongId();
        String userName = randomString();
        Date date = randomDate();
        LoginUser loginUser = randomPojo(LoginUser.class, o -> o.setUserType(randomEle(UserTypeEnum.values()).getValue()));
        // mock 方法
        when(securityProperties.getSessionTimeout()).thenReturn(Duration.ofDays(1));
        // mock 数据
        loginUser.setUpdateTime(date);
        loginUserCoreRedisDAO.set(sessionId, loginUser);
        SysUserSessionDO userSession = SysUserSessionDO.builder().id(sessionId)
                .userId(loginUser.getId()).userType(loginUser.getUserType())
                .userIp(userIp).userAgent(userAgent).username(userName)
                .sessionTimeout(addTime(Duration.ofMillis(timeLong)))
                .build();
        userSessionCoreMapper.insert(userSession);

        // 调用
        userSessionCoreService.refreshUserSession(sessionId, loginUser);
        // 校验 LoginUser 缓存
        LoginUser redisLoginUser = loginUserCoreRedisDAO.get(sessionId);
        assertNotEquals(redisLoginUser.getUpdateTime(), date);
        // 校验 SysUserSessionDO 记录
        SysUserSessionDO updateDO = userSessionCoreMapper.selectById(sessionId);
        assertEquals(updateDO.getUsername(), loginUser.getUsername());
        assertNotEquals(updateDO.getUpdateTime(), userSession.getUpdateTime());
        assertNotEquals(updateDO.getSessionTimeout(), addTime(Duration.ofMillis(timeLong)));
    }

    @Test
    public void testDeleteUserSession_success() {
        // 准备参数
        String sessionId = randomString();
        String userIp = randomString();
        String userAgent = randomString();
        Long timeLong = randomLongId();
        LoginUser loginUser = randomPojo(LoginUser.class, o -> o.setUserType(randomEle(UserTypeEnum.values()).getValue()));
        // mock 存入 Redis
        when(securityProperties.getSessionTimeout()).thenReturn(Duration.ofDays(1));
        // mock 数据
        loginUserCoreRedisDAO.set(sessionId, loginUser);
        SysUserSessionDO userSession = SysUserSessionDO.builder().id(sessionId)
                .userId(loginUser.getId()).userType(loginUser.getUserType())
                .userIp(userIp).userAgent(userAgent).username(loginUser.getUsername())
                .sessionTimeout(addTime(Duration.ofMillis(timeLong)))
                .build();
        userSessionCoreMapper.insert(userSession);

        // 调用
        userSessionCoreService.deleteUserSession(sessionId);
        // 校验数据不存在了
        assertNull(loginUserCoreRedisDAO.get(sessionId));
        assertNull(userSessionCoreMapper.selectById(sessionId));
    }

}
