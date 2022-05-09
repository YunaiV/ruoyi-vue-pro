package cn.iocoder.yudao.module.system.service.auth;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.security.config.SecurityProperties;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbAndRedisUnitTest;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.session.UserSessionPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.auth.UserSessionDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.auth.UserSessionMapper;
import cn.iocoder.yudao.module.system.dal.redis.auth.LoginUserRedisDAO;
import cn.iocoder.yudao.module.system.enums.common.SexEnum;
import cn.iocoder.yudao.module.system.service.logger.LoginLogService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.Duration;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private UserSessionMapper userSessionMapper;

    @MockBean
    private AdminUserService userService;
    @MockBean
    private LoginLogService loginLogService;
    @Resource
    private LoginUserRedisDAO loginUserRedisDAO;

    @MockBean
    private SecurityProperties securityProperties;

    @BeforeEach
    public void setUp() {
        when(securityProperties.getSessionTimeout()).thenReturn(Duration.ofDays(1L));
    }

    @Test
    public void testGetUserSessionPage_success() {
        // mock 数据
        AdminUserDO dbUser = randomPojo(AdminUserDO.class, o -> {
            o.setSex(randomEle(SexEnum.values()).getSex());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        when(userService.getUsersByUsername(eq(dbUser.getUsername()))).thenReturn(singletonList(dbUser));
        // 插入可被查询到的数据
        String userIp = randomString();
        UserSessionDO dbSession = randomPojo(UserSessionDO.class, o -> {
            o.setUserId(dbUser.getId());
            o.setUserType(randomEle(UserTypeEnum.values()).getValue());
            o.setUserIp(userIp);
        });
        userSessionMapper.insert(dbSession);
        // 测试 username 不匹配
        userSessionMapper.insert(ObjectUtils.cloneIgnoreId(dbSession, o -> o.setUserId(123456L)));
        // 测试 userIp 不匹配
        userSessionMapper.insert(ObjectUtils.cloneIgnoreId(dbSession, o -> o.setUserIp("testUserIp")));
        // 准备参数
        UserSessionPageReqVO reqVO = new UserSessionPageReqVO();
        reqVO.setUsername(dbUser.getUsername());
        reqVO.setUserIp(userIp);

        // 调用
        PageResult<UserSessionDO> pageResult = userSessionService.getUserSessionPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbSession, pageResult.getList().get(0));
    }

    @Test
    public void testDeleteUserSession_Token() {
        // 准备参数
        String token = randomString();

        // mock redis 数据
        loginUserRedisDAO.set(token, new LoginUser());
        // mock db 数据
        UserSessionDO userSession = randomPojo(UserSessionDO.class, o -> {
            o.setUserType(randomEle(UserTypeEnum.values()).getValue());
            o.setToken(token);
        });
        userSessionMapper.insert(userSession);

        // 调用
        userSessionService.deleteUserSession(token);
        // 校验数据不存在了
        assertNull(loginUserRedisDAO.get(token));
        assertNull(userSessionMapper.selectOne(UserSessionDO::getToken, token));
    }

    @Test
    public void testDeleteUserSession_Id() {
        // mock db 数据
        UserSessionDO userSession = randomPojo(UserSessionDO.class, o -> {
            o.setUserType(randomEle(UserTypeEnum.values()).getValue());
        });
        userSessionMapper.insert(userSession);
        // mock redis 数据
        loginUserRedisDAO.set(userSession.getToken(), new LoginUser());

        // 准备参数
        Long id = userSession.getId();

        // 调用
        userSessionService.deleteUserSession(id);
        // 校验数据不存在了
        assertNull(loginUserRedisDAO.get(userSession.getToken()));
        assertNull(userSessionMapper.selectById(id));
    }

}
