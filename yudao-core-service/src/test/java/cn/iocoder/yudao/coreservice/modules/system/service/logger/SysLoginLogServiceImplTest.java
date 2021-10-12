package cn.iocoder.yudao.coreservice.modules.system.service.logger;

import cn.iocoder.yudao.coreservice.BaseDbUnitTest;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.logger.SysLoginLogDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.mysql.logger.SysLoginLogCoreMapper;
import cn.iocoder.yudao.coreservice.modules.system.enums.logger.SysLoginLogTypeEnum;
import cn.iocoder.yudao.coreservice.modules.system.enums.logger.SysLoginResultEnum;
import cn.iocoder.yudao.coreservice.modules.system.service.logger.dto.SysLoginLogCreateReqDTO;
import cn.iocoder.yudao.coreservice.modules.system.service.logger.impl.SysLoginLogCoreServiceImpl;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.util.monitor.TracerUtils;
import cn.iocoder.yudao.framework.test.core.util.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;

@Import(SysLoginLogCoreServiceImpl.class)
public class SysLoginLogServiceImplTest extends BaseDbUnitTest {

    @Resource
    private SysLoginLogCoreServiceImpl loginLogCoreService;

    @Resource
    private SysLoginLogCoreMapper loginLogCoreMapper;

    @Test
    public void testCreateLoginLog() {
        SysLoginLogCreateReqDTO reqDTO = RandomUtils.randomPojo(SysLoginLogCreateReqDTO.class, vo -> {
            // 指定随机的范围,避免超出范围入库失败
            vo.setUserType(randomEle(UserTypeEnum.values()).getValue());
            vo.setLogType(randomEle(SysLoginLogTypeEnum.values()).getType());
            vo.setResult(randomEle(SysLoginResultEnum.values()).getResult());
            vo.setTraceId(TracerUtils.getTraceId());
        });

        // 调用
        loginLogCoreService.createLoginLog(reqDTO);
        // 断言，忽略基本字段
        SysLoginLogDO sysLoginLogDO = loginLogCoreMapper.selectOne(null);
        assertPojoEquals(reqDTO, sysLoginLogDO);
    }

}
