package cn.iocoder.dashboard.modules.infra.service.job;

import cn.iocoder.dashboard.BaseSpringBootUnitTest;
import cn.iocoder.dashboard.modules.infra.controller.config.vo.InfConfigCreateReqVO;
import cn.iocoder.dashboard.modules.infra.controller.config.vo.InfConfigUpdateReqVO;
import cn.iocoder.dashboard.modules.infra.dal.dataobject.config.InfConfigDO;
import cn.iocoder.dashboard.modules.infra.dal.mysql.config.InfConfigMapper;
import cn.iocoder.dashboard.modules.infra.service.config.impl.InfConfigServiceImpl;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;

import static cn.iocoder.dashboard.modules.infra.enums.InfErrorCodeConstants.CONFIG_NOT_EXISTS;
import static cn.iocoder.dashboard.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.dashboard.util.AssertUtils.assertServiceException;
import static cn.iocoder.dashboard.util.RandomUtils.randomLongId;
import static cn.iocoder.dashboard.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
* {@link InfConfigServiceImpl} 的单元测试类
*
* @author 芋艿
*/
public class InfConfigServiceTest extends BaseSpringBootUnitTest {

    @Resource
    private InfConfigServiceImpl configService;

    @Resource
    private InfConfigMapper configMapper;

    @Test
    public void testCreateConfig_success() {
        // 准备参数
        InfConfigCreateReqVO reqVO = randomPojo(InfConfigCreateReqVO.class);

        // 调用
        Long configId = configService.createConfig(reqVO);
        // 断言
        assertNotNull(configId);
        // 校验记录的属性是否正确
        InfConfigDO config = configMapper.selectById(configId);
        assertPojoEquals(reqVO, config);
    }

    @Test
    public void testUpdateConfig_success() {
        // mock 数据
        InfConfigDO dbConfig = randomPojo(InfConfigDO.class);
        configMapper.insert(dbConfig);// @Sql: 先插入出一条存在的数据
        // 准备参数
        InfConfigUpdateReqVO reqVO = randomPojo(InfConfigUpdateReqVO.class, o -> {
            o.setId(dbConfig.getId()); // 设置更新的 ID
        });

        // 调用
        configService.updateConfig(reqVO);
        // 校验是否更新正确
        InfConfigDO config = configMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, config);
    }

    @Test
    public void testUpdateConfig_notExists() {
        // 准备参数
        InfConfigUpdateReqVO reqVO = randomPojo(InfConfigUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> configService.updateConfig(reqVO), CONFIG_NOT_EXISTS);
    }

    @Test
    public void testDeleteConfig_success() {
        // mock 数据
        InfConfigDO dbConfig = randomPojo(InfConfigDO.class);
        configMapper.insert(dbConfig);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbConfig.getId();

        // 调用
        configService.deleteConfig(id);
       // 校验数据不存在了
       assertNull(configMapper.selectById(id));
    }

    @Test
    public void testDeleteConfig_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> configService.deleteConfig(id), CONFIG_NOT_EXISTS);
   }

}
