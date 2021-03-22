package cn.iocoder.dashboard.modules.system.service.auth;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.dashboard.modules.system.dal.redis.SysRedisKeyConstants.LOGIN_USER;
import static cn.iocoder.dashboard.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.dashboard.util.RandomUtils.randomDate;
import static cn.iocoder.dashboard.util.RandomUtils.randomLongId;
import static cn.iocoder.dashboard.util.RandomUtils.randomPojo;
import static cn.iocoder.dashboard.util.RandomUtils.randomString;
import static cn.iocoder.dashboard.util.date.DateUtils.addTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

import cn.hutool.core.date.DateUtil;
import cn.iocoder.dashboard.BaseDbAndRedisUnitTest;
import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.security.config.SecurityProperties;
import cn.iocoder.dashboard.framework.security.core.LoginUser;
import cn.iocoder.dashboard.modules.infra.controller.job.vo.job.InfJobPageReqVO;
import cn.iocoder.dashboard.modules.infra.dal.dataobject.job.InfJobDO;
import cn.iocoder.dashboard.modules.infra.enums.config.InfConfigTypeEnum;
import cn.iocoder.dashboard.modules.infra.enums.job.InfJobStatusEnum;
import cn.iocoder.dashboard.modules.system.controller.auth.vo.session.SysUserSessionPageReqVO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.auth.SysUserSessionDO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.auth.SysUserSessionMapper;
import cn.iocoder.dashboard.modules.system.dal.mysql.user.SysUserMapper;
import cn.iocoder.dashboard.modules.system.dal.redis.auth.SysLoginUserRedisDAO;
import cn.iocoder.dashboard.modules.system.enums.common.SysSexEnum;
import cn.iocoder.dashboard.modules.system.service.auth.impl.SysUserSessionServiceImpl;
import cn.iocoder.dashboard.modules.system.service.dept.impl.SysDeptServiceImpl;
import cn.iocoder.dashboard.modules.system.service.logger.impl.SysLoginLogServiceImpl;
import cn.iocoder.dashboard.modules.system.service.user.SysUserServiceImpl;
import cn.iocoder.dashboard.util.AssertUtils;
import cn.iocoder.dashboard.util.RandomUtils;
import cn.iocoder.dashboard.util.json.JsonUtils;
import cn.iocoder.dashboard.util.object.ObjectUtils;

/**
 * SysUserSessionServiceImpl Tester.
 *
 * @author Lyon
 * @version 1.0
 * @since <pre>3月 8, 2021</pre>
 */
@Import({SysUserSessionServiceImpl.class, SysLoginUserRedisDAO.class})
public class SysUserSessionServiceImplTest extends BaseDbAndRedisUnitTest {

    @Resource
    private SysUserSessionServiceImpl sysUserSessionService;
    @Resource
    private SysUserSessionMapper sysUserSessionMapper;
    @Resource
    private SysLoginUserRedisDAO sysLoginUserRedisDAO;
    @Resource
    private SysUserMapper sysUserMapper;

    @MockBean
    private SecurityProperties securityProperties;
    @MockBean
    private SysDeptServiceImpl sysDeptService;
    @MockBean
    private SysUserServiceImpl sysUserService;
    @MockBean
    private SysLoginLogServiceImpl sysLoginLogService;

    @Test
    public void testCreateUserSession_success() {
        // 准备参数
        String userIp = randomString();
        String userAgent = randomString();
        LoginUser loginUser = randomPojo(LoginUser.class);
        // mock
        when(securityProperties.getSessionTimeout()).thenReturn(Duration.ofDays(1));
        // 调用
        String sessionId = sysUserSessionService.createUserSession(loginUser, userIp, userAgent);
        // 校验记录的属性是否正确
        SysUserSessionDO sysUserSessionDO = sysUserSessionMapper.selectById(sessionId);
        assertEquals(sysUserSessionDO.getId(), sessionId);
        assertEquals(sysUserSessionDO.getUserId(), loginUser.getId());
        assertEquals(sysUserSessionDO.getUserIp(), userIp);
        assertEquals(sysUserSessionDO.getUserAgent(), userAgent);
        assertEquals(sysUserSessionDO.getUsername(), loginUser.getUsername());
        LoginUser redisLoginUser = sysLoginUserRedisDAO.get(sessionId);
        AssertUtils.assertPojoEquals(redisLoginUser, loginUser, "username","password");
    }

    @Test
    public void testCreateRefreshUserSession_success() {
        // 准备参数
        String sessionId = randomString();
        String userIp = randomString();
        String userAgent = randomString();
        Long timeLong = randomLongId();
        String userName = randomString();
        Date date = randomDate();
        LoginUser loginUser = randomPojo(LoginUser.class);
        // mock
        when(securityProperties.getSessionTimeout()).thenReturn(Duration.ofDays(1));
        loginUser.setUpdateTime(date);
        sysLoginUserRedisDAO.set(sessionId, loginUser);
        SysUserSessionDO userSession = SysUserSessionDO.builder().id(sessionId)
                .userId(loginUser.getId()).userIp(userIp).userAgent(userAgent).username(userName)
                .sessionTimeout(addTime(Duration.ofMillis(timeLong)))
                .build();
        sysUserSessionMapper.insert(userSession);
        SysUserSessionDO insertDO = sysUserSessionMapper.selectById(sessionId);
        // 调用
        sysUserSessionService.refreshUserSession(sessionId, loginUser);
        // 校验记录 redis
        LoginUser redisLoginUser = sysLoginUserRedisDAO.get(sessionId);
        assertNotEquals(redisLoginUser.getUpdateTime(), date);
        // 校验记录 SysUserSessionDO
        SysUserSessionDO updateDO = sysUserSessionMapper.selectById(sessionId);
        assertEquals(updateDO.getUsername(), loginUser.getUsername());
        assertNotEquals(updateDO.getUpdateTime(), insertDO.getUpdateTime());
        assertNotEquals(updateDO.getSessionTimeout(), addTime(Duration.ofMillis(timeLong)));
    }

    @Test
    public void testDeleteUserSession_success() {
        // 准备参数
        String sessionId = randomString();
        String userIp = randomString();
        String userAgent = randomString();
        Long timeLong = randomLongId();
        LoginUser loginUser = randomPojo(LoginUser.class);
        // mock 存入 Redis
        when(securityProperties.getSessionTimeout()).thenReturn(Duration.ofDays(1));
        sysLoginUserRedisDAO.set(sessionId, loginUser);
        // mock 存入 db
        SysUserSessionDO userSession = SysUserSessionDO.builder().id(sessionId)
                .userId(loginUser.getId()).userIp(userIp).userAgent(userAgent).username(loginUser.getUsername())
                .sessionTimeout(addTime(Duration.ofMillis(timeLong)))
                .build();
        sysUserSessionMapper.insert(userSession);
        // 校验数据存在
        assertNotNull(sysLoginUserRedisDAO.get(sessionId));
        assertNotNull(sysUserSessionMapper.selectById(sessionId));
        // 调用
        sysUserSessionService.deleteUserSession(sessionId);
        // 校验数据不存在了
        assertNull(sysLoginUserRedisDAO.get(sessionId));
        assertNull(sysUserSessionMapper.selectById(sessionId));
    }

    @Test
    public void testGetUserSessionPage_success() {
        // mock 数据
        String userIp = randomString();
        SysUserDO dbUser1 = randomPojo(SysUserDO.class, o -> {
            o.setUsername("testUsername1");
            o.setSex(randomEle(SysSexEnum.values()).getSEX());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        SysUserDO dbUser2 = randomPojo(SysUserDO.class, o -> {
            o.setUsername("testUsername2");
            o.setSex(randomEle(SysSexEnum.values()).getSEX());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        SysUserSessionDO dbSession = randomPojo(SysUserSessionDO.class, o -> {
            o.setUserId(dbUser1.getId());
            o.setUserIp(userIp);
        });
        sysUserMapper.insert(dbUser1);
        sysUserMapper.insert(dbUser2);
        sysUserSessionMapper.insert(dbSession);
        sysUserSessionMapper.insert(ObjectUtils.clone(dbSession, o -> {
            o.setId(randomString());
            o.setUserId(dbUser2.getId());
        }));
        // 测试 userId 不匹配
        sysUserSessionMapper.insert(ObjectUtils.clone(dbSession, o -> {
            o.setId(randomString());
            o.setUserId(123456l);
        }));
        // 测试 userIp 不匹配
        sysUserSessionMapper.insert(ObjectUtils.clone(dbSession, o -> {
            o.setId(randomString());
            o.setUserIp("testUserIp");
        }));
        // 准备参数
        SysUserSessionPageReqVO reqVo = new SysUserSessionPageReqVO();
        reqVo.setUserIp(userIp);
        // 调用
        PageResult<SysUserSessionDO> pageResult = sysUserSessionService.getUserSessionPage(reqVo);
        // 断言
        assertEquals(3, pageResult.getTotal());
        assertEquals(3, pageResult.getList().size());
        assertPojoEquals(dbSession, pageResult.getList().get(0));
    }

    @Test
    public void testClearSessionTimeout_success() throws Exception {
        // 准备超时数据 120 条, 在线用户 1 条
        int expectedTimeoutCount = 120, expectedTotal = 1;

        // 准备数据
        List<SysUserSessionDO> prepareData = Stream
                .iterate(0, i -> i)
                .limit(expectedTimeoutCount)
                .map(i -> RandomUtils.randomPojo(SysUserSessionDO.class, o -> o.setSessionTimeout(DateUtil.offsetSecond(new Date(), -1))))
                .collect(Collectors.toList());
        SysUserSessionDO sessionDO = RandomUtils.randomPojo(SysUserSessionDO.class, o -> o.setSessionTimeout(DateUtil.offsetMinute(new Date(), 30)));
        prepareData.add(sessionDO);
        prepareData.forEach(sysUserSessionMapper::insert);

        //清空超时数据
        long actualTimeoutCount = sysUserSessionService.clearSessionTimeout();
        //校验
        assertEquals(expectedTimeoutCount, actualTimeoutCount);
        List<SysUserSessionDO> userSessionDOS = sysUserSessionMapper.selectList();
        assertEquals(expectedTotal, userSessionDOS.size());
        AssertUtils.assertPojoEquals(sessionDO, userSessionDOS.get(0), "updateTime");
    }

}
