package cn.iocoder.yudao.module.system.service.auth;

import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.module.system.controller.auth.vo.session.SysUserSessionPageReqVO;
import cn.iocoder.yudao.module.system.dal.mysql.auth.SysUserSessionMapper;
import cn.iocoder.yudao.module.system.service.user.SysUserService;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.auth.SysUserSessionDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.redis.auth.SysLoginUserCoreRedisDAO;
import cn.iocoder.yudao.coreservice.modules.system.enums.common.SysSexEnum;
import cn.iocoder.yudao.coreservice.modules.system.service.logger.SysLoginLogCoreService;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.system.test.BaseDbAndRedisUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link SysUserSessionServiceImpl} 的单元测试
 *
 * @author Lyon
 */
@Import({SysUserSessionServiceImpl.class})
public class SysUserSessionServiceImplTest extends BaseDbAndRedisUnitTest {

    @Resource
    private SysUserSessionServiceImpl userSessionService;

    @Resource
    private SysUserSessionMapper userSessionMapper;

    @MockBean
    private SysUserService userService;
    @MockBean
    private SysLoginLogCoreService loginLogCoreService;
    @MockBean
    private SysLoginUserCoreRedisDAO loginUserCoreRedisDAO;

    @Test
    public void testGetUserSessionPage_success() {
        // mock 数据
        SysUserDO dbUser = randomPojo(SysUserDO.class, o -> {
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
        SysUserSessionPageReqVO reqVO = new SysUserSessionPageReqVO();
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

}
