package cn.iocoder.dashboard.modules.infra.service.config;

import cn.iocoder.dashboard.BaseSpringBootUnitTest;
import cn.iocoder.dashboard.common.exception.ServiceException;
import cn.iocoder.dashboard.modules.infra.controller.config.vo.InfConfigCreateReqVO;
import cn.iocoder.dashboard.modules.infra.controller.config.vo.InfConfigUpdateReqVO;
import cn.iocoder.dashboard.modules.infra.dal.dataobject.config.InfConfigDO;
import cn.iocoder.dashboard.modules.infra.dal.mysql.config.InfConfigMapper;
import cn.iocoder.dashboard.modules.infra.enums.config.InfConfigTypeEnum;
import cn.iocoder.dashboard.modules.infra.mq.producer.config.InfConfigProducer;
import cn.iocoder.dashboard.modules.infra.service.config.impl.InfConfigServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.function.Consumer;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.dashboard.modules.infra.enums.InfErrorCodeConstants.CONFIG_KEY_DUPLICATE;
import static cn.iocoder.dashboard.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.dashboard.util.RandomUtils.randomPojo;
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
        // 准备参数
        InfConfigCreateReqVO reqVO = randomInfConfigCreateReqVO();
        // mock

        // 调用
        Long configId = configService.createConfig(reqVO);
        // 断言
        assertNotNull(configId);
        // 校验记录的属性是否正确
        InfConfigDO config = configMapper.selectById(configId);
        assertPojoEquals(reqVO, config);
        assertEquals(InfConfigTypeEnum.CUSTOM.getType(), config.getType());
        // 校验调用
        verify(configProducer, times(1)).sendConfigRefreshMessage();
    }

    @Test
    public void testCreateConfig_keyDuplicate() {
        // 准备参数
        InfConfigCreateReqVO reqVO = randomInfConfigCreateReqVO();
        // mock 数据
        configMapper.insert(randomInfConfigDO(o -> { // @Sql
            o.setKey(reqVO.getKey()); // 模拟 key 重复
        }));

        // 调用
        ServiceException serviceException = assertThrows(ServiceException.class, () -> configService.createConfig(reqVO));
        // 断言
        assertPojoEquals(CONFIG_KEY_DUPLICATE, serviceException);
    }

    @Test
    public void testUpdateConfig_success() {
        // 准备参数
        InfConfigUpdateReqVO reqVO = randomInfConfigUpdateReqVO();
        // mock 数据
        configMapper.insert(randomInfConfigDO(o -> o.setId(reqVO.getId())));// @Sql: 先插入出一条存在的数据

        // 调用
        configService.updateConfig(reqVO);
        // 校验是否更新正确
        InfConfigDO config = configMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, config);
        // 校验调用
        verify(configProducer, times(1)).sendConfigRefreshMessage();
    }

    // ========== 随机对象 ==========

    @SafeVarargs
    private static InfConfigDO randomInfConfigDO(Consumer<InfConfigDO>... consumers) {
        InfConfigDO config = randomPojo(InfConfigDO.class, consumers);
        config.setType(randomEle(InfConfigTypeEnum.values()).getType());
        return config;
    }

    private static InfConfigCreateReqVO randomInfConfigCreateReqVO() {
        return randomPojo(InfConfigCreateReqVO.class);
    }

    private static InfConfigUpdateReqVO randomInfConfigUpdateReqVO() {
        return randomPojo(InfConfigUpdateReqVO.class);
    }

}
