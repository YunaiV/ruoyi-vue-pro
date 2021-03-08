package cn.iocoder.dashboard.modules.system.service.auth;

import cn.hutool.core.date.DateUtil;
import cn.iocoder.dashboard.BaseSpringBootUnitTest;
import cn.iocoder.dashboard.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.dashboard.modules.system.dal.dataobject.auth.SysUserSessionDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.auth.SysUserSessionMapper;
import cn.iocoder.dashboard.util.RandomUtils;
import org.junit.jupiter.api.Test;

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
public class SysUserSessionServiceImplTest extends BaseSpringBootUnitTest {

    @Resource
    SysUserSessionService sysUserSessionService;
    @Resource
    SysUserSessionMapper sysUserSessionMapper;

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
        prepareData.add(RandomUtils.randomPojo(SysUserSessionDO.class, o -> o.setSessionTimeout(DateUtil.offsetMinute(new Date(), 30))));
        prepareData.forEach(sysUserSessionMapper::insert);

        //清空超时数据
        long actualTimeoutCount = sysUserSessionService.clearSessionTimeout();
        assertEquals(expectedTimeoutCount, actualTimeoutCount);
        Integer actualTotal = sysUserSessionMapper.selectCount(new QueryWrapperX<>());
        assertEquals(expectedTotal, actualTotal);
    }

} 
