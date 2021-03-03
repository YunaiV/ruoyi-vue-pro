package cn.iocoder.dashboard.modules.infra.service.config;

import cn.iocoder.dashboard.BaseSpringBootUnitTest;
import cn.iocoder.dashboard.common.exception.ServiceException;
import cn.iocoder.dashboard.modules.infra.controller.config.vo.InfConfigCreateReqVO;
import cn.iocoder.dashboard.modules.infra.dal.dataobject.config.InfConfigDO;
import cn.iocoder.dashboard.modules.infra.dal.mysql.config.InfConfigMapper;
import cn.iocoder.dashboard.modules.infra.enums.config.InfConfigTypeEnum;
import cn.iocoder.dashboard.modules.infra.mq.producer.config.InfConfigProducer;
import cn.iocoder.dashboard.modules.infra.service.config.impl.InfConfigServiceImpl;
import cn.iocoder.dashboard.util.AssertUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.dashboard.modules.infra.enums.InfErrorCodeConstants.CONFIG_KEY_DUPLICATE;
import static cn.iocoder.dashboard.util.RandomUtils.randomInfConfigCreateReqVO;
import static cn.iocoder.dashboard.util.RandomUtils.randomInfConfigDO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class InfConfigServiceImplTest extends BaseSpringBootUnitTest {

    @Resource
    private InfConfigServiceImpl configService;

    @Resource
    private InfConfigMapper configMapper;

    @MockBean
    private InfConfigProducer configProducer;

    @Test
    public void testCreateConfig_success() {
        // 入参
        InfConfigCreateReqVO reqVO = randomInfConfigCreateReqVO();
        // mock

        // 调用
        Long configId = configService.createConfig(reqVO);
        // 校验
        assertNotNull(configId);
        // 校验记录的属性是否正确
        InfConfigDO config = configMapper.selectById(configId);
        AssertUtils.assertEquals(reqVO, config);
        assertEquals(InfConfigTypeEnum.CUSTOM.getType(), config.getType());
        // 校验调用
        verify(configProducer, times(1)).sendConfigRefreshMessage();
    }

    @Test
    public void testCreateConfig_keyDuplicate() {
        // 入参
        InfConfigCreateReqVO reqVO = randomInfConfigCreateReqVO();
        // mock 数据
        configMapper.insert(randomInfConfigDO(o -> {
            o.setKey(reqVO.getKey()); // @Sql：插入一条重复的 key
            o.setType(randomEle(InfConfigTypeEnum.values()).getType());
        }));

        // 调用
        ServiceException serviceException = assertThrows(ServiceException.class, () -> configService.createConfig(reqVO));
        // 断言
        AssertUtils.assertEquals(CONFIG_KEY_DUPLICATE, serviceException);
    }

}
