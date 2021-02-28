package cn.iocoder.dashboard.modules.infra.service.config;

import cn.iocoder.dashboard.common.exception.ServiceException;
import cn.iocoder.dashboard.modules.infra.controller.config.vo.InfConfigCreateReqVO;
import cn.iocoder.dashboard.modules.infra.dal.dataobject.config.InfConfigDO;
import cn.iocoder.dashboard.modules.infra.dal.mysql.config.InfConfigMapper;
import cn.iocoder.dashboard.modules.infra.enums.config.InfConfigTypeEnum;
import cn.iocoder.dashboard.modules.infra.mq.producer.config.InfConfigProducer;
import cn.iocoder.dashboard.modules.infra.service.config.impl.InfConfigServiceImpl;
import cn.iocoder.dashboard.util.AssertUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import javax.annotation.Resource;

import static cn.iocoder.dashboard.modules.infra.enums.InfErrorCodeConstants.CONFIG_KEY_DUPLICATE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("unit-test")
@Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class InfConfigServiceImplTest {

    @Resource
    private InfConfigServiceImpl configService;

    @Resource
    private InfConfigMapper configMapper;

    @MockBean
    private InfConfigProducer configProducer;

    @Test
    public void testCreateConfig_success() {
        // 入参
        InfConfigCreateReqVO reqVO = new InfConfigCreateReqVO();
        reqVO.setGroup("test_group");
        reqVO.setName("test_name");
        reqVO.setValue("test_value");
        reqVO.setSensitive(true);
        reqVO.setRemark("test_remark");
        reqVO.setKey("test_key");
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
    @Sql(statements = "INSERT INTO `inf_config`(`group`, `type`, `name`, `key`, `value`, `sensitive`)  VALUES ('test_group', 1, 'test_name', 'test_key', 'test_value', 1);")
    public void testCreateConfig_keyDuplicate() {
        // 入参
        InfConfigCreateReqVO reqVO = new InfConfigCreateReqVO();
        reqVO.setGroup("test_group");
        reqVO.setName("test_name");
        reqVO.setValue("test_value");
        reqVO.setSensitive(true);
        reqVO.setRemark("test_remark");
        reqVO.setKey("test_key");
        // mock

        // 调用
        ServiceException serviceException = assertThrows(ServiceException.class, () -> configService.createConfig(reqVO));
        // 断言
        AssertUtils.assertEquals(CONFIG_KEY_DUPLICATE, serviceException);
    }

}
