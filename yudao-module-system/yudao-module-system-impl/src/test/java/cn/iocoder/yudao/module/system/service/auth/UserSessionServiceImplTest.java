package cn.iocoder.yudao.module.system.service.auth;

import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.security.config.SecurityProperties;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.session.UserSessionPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.auth.SysUserSessionMapper;
import cn.iocoder.yudao.module.system.service.logger.LoginLogService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import cn.iocoder.yudao.module.system.dal.dataobject.auth.SysUserSessionDO;
import cn.iocoder.yudao.module.system.dal.redis.auth.LoginUserRedisDAO;
import cn.iocoder.yudao.module.system.enums.common.SysSexEnum;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.system.test.BaseDbAndRedisUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.addTime;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link UserSessionServiceImpl} 的单元测试
 *
 * @author Lyon
 */
@Import({UserSessionServiceImpl.class, LoginUserRedisDAO.class})
public class UserSessionServiceImplTest extends BaseDbAndRedisUnitTest {

    @Resource
    private UserSessionServiceImpl userSessionService;

    @Resource
    private SysUserSessionMapper userSessionMapper;

    @MockBean
    private AdminUserService userService;
    @MockBean
    private LoginLogService loginLogService;
    @MockBean
    private LoginUserRedisDAO loginUserRedisDAO;

    @MockBean
    private SecurityProperties securityProperties;

    @Test
    public void testGetUserSessionPage_success() {
        // mock 数据
        AdminUserDO dbUser = randomPojo(AdminUserDO.class, o -> {
            o.setSex(randomEle(SysSexEnum.values()).getSex());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        when(userService.getUsersByUsername(eq(dbUser.getUsername()))).thenReturn(singletonList(dbUser));
        // 插入可被查询到的数据
        String userIp = randomString();
        SysUserSessionDO dbSession = randomPojo(SysUserSessionDO.class, o -> {
            o.setUserId(dbUser.getId());
            o.setUserType(randomEle(UserTypeEnum.values()).getValue());
            o.setUserIp(userIp);
        });
        userSessionMapper.insert(dbSession);
        // 测试 username 不匹配
        userSessionMapper.insert(ObjectUtils.cloneIgnoreId(dbSession, o -> {
            o.setId(randomString());
            o.setUserId(123456L);
        }));
        // 测试 userIp 不匹配
        userSessionMapper.insert(ObjectUtils.cloneIgnoreId(dbSession, o -> {
            o.setId(randomString());
            o.setUserIp("testUserIp");
        }));
        // 准备参数
        UserSessionPageReqVO reqVO = new UserSessionPageReqVO();
        reqVO.setUsername(dbUser.getUsername());
        reqVO.setUserIp(userIp);

        // 调用
        PageResult<SysUserSessionDO> pageResult = userSessionService.getUserSessionPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbSession, pageResult.getList().get(0));
    }

    // TODO 芋艿：单测写的有问题
    @Test
    public void testClearSessionTimeout_success() {
        // 准备超时数据 120 条, 在线用户 1 条
        int expectedTimeoutCount = 120, expectedTotal = 1;

        // 准备数据
        List<SysUserSessionDO> prepareData = Stream
                .iterate(0, i -> i)
                .limit(expectedTimeoutCount)
                .map(i -> randomPojo(SysUserSessionDO.class, o -> {
                    o.setUserType(randomEle(UserTypeEnum.values()).getValue());
                    o.setSessionTimeout(DateUtil.offsetSecond(new Date(), -1));
                }))
                .collect(Collectors.toList());
        SysUserSessionDO sessionDO = randomPojo(SysUserSessionDO.class, o -> {
            o.setUserType(randomEle(UserTypeEnum.values()).getValue());
            o.setSessionTimeout(DateUtil.offsetMinute(new Date(), 30));
        });
        prepareData.add(sessionDO);
        prepareData.forEach(userSessionMapper::insert);

        // 清空超时数据
        long actualTimeoutCount = userSessionService.clearSessionTimeout();
        // 校验
        assertEquals(expectedTimeoutCount, actualTimeoutCount);
        List<SysUserSessionDO> userSessionDOS = userSessionMapper.selectList();
        assertEquals(expectedTotal, userSessionDOS.size());
        assertPojoEquals(sessionDO, userSessionDOS.get(0), "updateTime");
    }

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
        String sessionId = userSessionService.createUserSession(loginUser, userIp, userAgent);
        // 校验 SysUserSessionDO 记录
        SysUserSessionDO userSessionDO = userSessionMapper.selectById(sessionId);
        assertPojoEquals(loginUser, userSessionDO, "id", "updateTime");
        assertEquals(sessionId, userSessionDO.getId());
        assertEquals(userIp, userSessionDO.getUserIp());
        assertEquals(userAgent, userSessionDO.getUserAgent());
        // 校验 LoginUser 缓存
        LoginUser redisLoginUser = loginUserRedisDAO.get(sessionId);
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
        loginUserRedisDAO.set(sessionId, loginUser);
        SysUserSessionDO userSession = SysUserSessionDO.builder().id(sessionId)
                .userId(loginUser.getId()).userType(loginUser.getUserType())
                .userIp(userIp).userAgent(userAgent).username(userName)
                .sessionTimeout(addTime(Duration.ofMillis(timeLong)))
                .build();
        userSessionMapper.insert(userSession);

        // 调用
        userSessionService.refreshUserSession(sessionId, loginUser);
        // 校验 LoginUser 缓存
        LoginUser redisLoginUser = loginUserRedisDAO.get(sessionId);
        assertNotEquals(redisLoginUser.getUpdateTime(), date);
        // 校验 SysUserSessionDO 记录
        SysUserSessionDO updateDO = userSessionMapper.selectById(sessionId);
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
        loginUserRedisDAO.set(sessionId, loginUser);
        SysUserSessionDO userSession = SysUserSessionDO.builder().id(sessionId)
                .userId(loginUser.getId()).userType(loginUser.getUserType())
                .userIp(userIp).userAgent(userAgent).username(loginUser.getUsername())
                .sessionTimeout(addTime(Duration.ofMillis(timeLong)))
                .build();
        userSessionMapper.insert(userSession);

        // 调用
        userSessionService.deleteUserSession(sessionId);
        // 校验数据不存在了
        assertNull(loginUserRedisDAO.get(sessionId));
        assertNull(userSessionMapper.selectById(sessionId));
    }

}
