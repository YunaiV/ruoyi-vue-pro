package cn.iocoder.dashboard.modules.system.service.auth;

import cn.hutool.core.date.DateUtil;
import cn.iocoder.dashboard.BaseDbAndRedisUnitTest;
import cn.iocoder.dashboard.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.dashboard.framework.security.config.SecurityProperties;
import cn.iocoder.dashboard.modules.system.dal.dataobject.auth.SysUserSessionDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.auth.SysUserSessionMapper;
import cn.iocoder.dashboard.modules.system.dal.redis.auth.SysLoginUserRedisDAO;
import cn.iocoder.dashboard.modules.system.service.auth.impl.SysUserSessionServiceImpl;
import cn.iocoder.dashboard.modules.system.service.dept.impl.SysDeptServiceImpl;
import cn.iocoder.dashboard.modules.system.service.logger.impl.SysLoginLogServiceImpl;
import cn.iocoder.dashboard.modules.system.service.user.SysUserServiceImpl;
import cn.iocoder.dashboard.util.AssertUtils;
import cn.iocoder.dashboard.util.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * SysUserSessionServiceImpl Tester.
 *
 * @author Lyon
 * @version 1.0
 * @since <pre>3月 8, 2021</pre>
 */
@Import(
        SysUserSessionServiceImpl.class)
public class SysUserSessionServiceImplTest extends BaseDbAndRedisUnitTest {

    @Resource
    SysUserSessionServiceImpl sysUserSessionService;
    @Resource
    SysUserSessionMapper sysUserSessionMapper;
    @MockBean
    SecurityProperties securityProperties;
    @MockBean
    SysDeptServiceImpl sysDeptService;
    @MockBean
    SysUserServiceImpl sysUserService;
    @MockBean
    SysLoginLogServiceImpl sysLoginLogService;
    @MockBean
    SysLoginUserRedisDAO sysLoginUserRedisDAO;

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
