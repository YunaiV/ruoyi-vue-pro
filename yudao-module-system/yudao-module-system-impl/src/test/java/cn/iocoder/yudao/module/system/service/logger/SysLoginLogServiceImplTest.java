package cn.iocoder.yudao.module.system.service.logger;

import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.logger.SysLoginLogDO;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.monitor.TracerUtils;
import cn.iocoder.yudao.module.system.controller.logger.vo.loginlog.SysLoginLogExportReqVO;
import cn.iocoder.yudao.module.system.controller.logger.vo.loginlog.SysLoginLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.mysql.logger.SysLoginLogMapper;
import cn.iocoder.yudao.module.system.enums.logger.SysLoginLogTypeEnum;
import cn.iocoder.yudao.module.system.enums.logger.SysLoginResultEnum;
import cn.iocoder.yudao.framework.test.core.util.RandomUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.system.test.BaseDbUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.buildTime;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Import(SysLoginLogServiceImpl.class)
public class SysLoginLogServiceImplTest extends BaseDbUnitTest {

    @Resource
    private SysLoginLogServiceImpl sysLoginLogService;

    @Resource
    private SysLoginLogMapper loginLogMapper;

    @Test
    public void testGetLoginLogPage() {
        // 构造测试数据
        // 登录成功的
        SysLoginLogDO loginLogDO = RandomUtils.randomPojo(SysLoginLogDO.class, logDO -> {
            logDO.setLogType(RandomUtil.randomEle(SysLoginLogTypeEnum.values()).getType());
            logDO.setTraceId(TracerUtils.getTraceId());
            logDO.setUserType(RandomUtil.randomEle(UserTypeEnum.values()).getValue());

            logDO.setUserIp("192.168.199.16");
            logDO.setUsername("wangkai");
            logDO.setCreateTime(buildTime(2021, 3, 6));
            logDO.setResult(SysLoginResultEnum.SUCCESS.getResult());
        });
        loginLogMapper.insert(loginLogDO);

        // 下面几个都是不匹配的数据
        // 登录失败的
        loginLogMapper.insert(ObjectUtils.cloneIgnoreId(loginLogDO, logDO -> logDO.setResult(SysLoginResultEnum.CAPTCHA_CODE_ERROR.getResult())));
        // 不同ip段的
        loginLogMapper.insert(ObjectUtils.cloneIgnoreId(loginLogDO, logDO -> logDO.setUserIp("192.168.128.18")));
        // 不同username
        loginLogMapper.insert(ObjectUtils.cloneIgnoreId(loginLogDO, logDO -> logDO.setUsername("yunai")));
        // 构造一个早期时间 2021-02-06 00:00:00
        loginLogMapper.insert(ObjectUtils.cloneIgnoreId(loginLogDO, logDO -> logDO.setCreateTime(buildTime(2021, 2, 6))));


        // 构造调用参数
        SysLoginLogPageReqVO reqVO = new SysLoginLogPageReqVO();
        reqVO.setUsername("wangkai");
        reqVO.setUserIp("192.168.199");
        reqVO.setStatus(true);
        reqVO.setBeginTime(buildTime(2021, 3, 5));
        reqVO.setEndTime(buildTime(2021, 3, 7));

        // 调用service方法
        PageResult<SysLoginLogDO> pageResult = sysLoginLogService.getLoginLogPage(reqVO);

        // 断言，只查到了一条符合条件的
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(loginLogDO, pageResult.getList().get(0));
    }

    @Test
    public void testGetLoginLogList() {
        // 构造测试数据

        // 登录成功的
        SysLoginLogDO loginLogDO = RandomUtils.randomPojo(SysLoginLogDO.class, logDO -> {
            logDO.setLogType(RandomUtil.randomEle(SysLoginLogTypeEnum.values()).getType());
            logDO.setTraceId(TracerUtils.getTraceId());
            logDO.setUserType(RandomUtil.randomEle(UserTypeEnum.values()).getValue());

            logDO.setUserIp("192.168.111.16");
            logDO.setUsername("wangxiaokai");
            logDO.setCreateTime(buildTime(2021, 3, 6));
            logDO.setResult(SysLoginResultEnum.SUCCESS.getResult());
        });
        loginLogMapper.insert(loginLogDO);

        // 下面几个都是不匹配的数据
        // 登录失败的
        loginLogMapper.insert(ObjectUtils.cloneIgnoreId(loginLogDO, logDO -> logDO.setResult(SysLoginResultEnum.CAPTCHA_CODE_ERROR.getResult())));
        // 不同ip段的
        loginLogMapper.insert(ObjectUtils.cloneIgnoreId(loginLogDO, logDO -> logDO.setUserIp("192.168.128.18")));
        // 不同username
        loginLogMapper.insert(ObjectUtils.cloneIgnoreId(loginLogDO, logDO -> logDO.setUsername("yunai")));
        // 构造一个早期时间 2021-02-06 00:00:00
        loginLogMapper.insert(ObjectUtils.cloneIgnoreId(loginLogDO, logDO -> logDO.setCreateTime(buildTime(2021, 2, 6))));


        // 构造调用参数
        SysLoginLogExportReqVO reqVO = new SysLoginLogExportReqVO();
        reqVO.setUsername("wangxiaokai");
        reqVO.setUserIp("192.168.111");
        reqVO.setStatus(true);
        reqVO.setBeginTime(buildTime(2021, 3, 5));
        reqVO.setEndTime(buildTime(2021, 3, 7));


        // 调用service方法
        List<SysLoginLogDO> loginLogList = sysLoginLogService.getLoginLogList(reqVO);

        // 断言
        assertEquals(1, loginLogList.size());
        assertPojoEquals(loginLogDO, loginLogList.get(0));
    }
}
