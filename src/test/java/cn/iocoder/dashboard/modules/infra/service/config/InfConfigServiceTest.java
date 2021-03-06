package cn.iocoder.dashboard.modules.infra.service.config;

import cn.iocoder.dashboard.BaseSpringBootUnitTest;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.infra.controller.config.vo.InfConfigCreateReqVO;
import cn.iocoder.dashboard.modules.infra.controller.config.vo.InfConfigExportReqVO;
import cn.iocoder.dashboard.modules.infra.controller.config.vo.InfConfigPageReqVO;
import cn.iocoder.dashboard.modules.infra.controller.config.vo.InfConfigUpdateReqVO;
import cn.iocoder.dashboard.modules.infra.dal.dataobject.config.InfConfigDO;
import cn.iocoder.dashboard.modules.infra.dal.mysql.config.InfConfigMapper;
import cn.iocoder.dashboard.modules.infra.enums.config.InfConfigTypeEnum;
import cn.iocoder.dashboard.modules.infra.mq.producer.config.InfConfigProducer;
import cn.iocoder.dashboard.modules.infra.service.config.impl.InfConfigServiceImpl;
import cn.iocoder.dashboard.util.collection.ArrayUtils;
import cn.iocoder.dashboard.util.object.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.List;
import java.util.function.Consumer;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.dashboard.modules.infra.enums.InfErrorCodeConstants.*;
import static cn.iocoder.dashboard.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.dashboard.util.AssertUtils.assertServiceException;
import static cn.iocoder.dashboard.util.RandomUtils.randomLongId;
import static cn.iocoder.dashboard.util.RandomUtils.randomPojo;
import static cn.iocoder.dashboard.util.date.DateUtils.buildTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * {@link InfConfigServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
public class InfConfigServiceTest extends BaseSpringBootUnitTest {

    @Resource
    private InfConfigServiceImpl configService;

    @Resource
    private InfConfigMapper configMapper;
    @MockBean
    private InfConfigProducer configProducer;

    @Test
    public void testGetConfigPage() {
        // mock 数据
        InfConfigDO dbConfig = randomInfConfigDO(o -> { // 等会查询到
            o.setName("芋艿");
            o.setKey("yunai");
            o.setType(InfConfigTypeEnum.SYSTEM.getType());
            o.setCreateTime(buildTime(2021, 2, 1));
        });
        configMapper.insert(dbConfig);
        // 测试 name 不匹配
        configMapper.insert(ObjectUtils.clone(dbConfig, o -> o.setName("土豆")));
        // 测试 key 不匹配
        configMapper.insert(ObjectUtils.clone(dbConfig, o -> o.setKey("tudou")));
        // 测试 type 不匹配
        configMapper.insert(ObjectUtils.clone(dbConfig, o -> o.setType(InfConfigTypeEnum.CUSTOM.getType())));
        // 测试 createTime 不匹配
        configMapper.insert(ObjectUtils.clone(dbConfig, o -> o.setCreateTime(buildTime(2021, 1, 1))));
        // 准备参数
        InfConfigPageReqVO reqVO = new InfConfigPageReqVO();
        reqVO.setName("艿");
        reqVO.setKey("nai");
        reqVO.setType(InfConfigTypeEnum.SYSTEM.getType());
        reqVO.setBeginTime(buildTime(2021, 1, 15));
        reqVO.setEndTime(buildTime(2021, 2, 15));

        // 调用
        PageResult<InfConfigDO> pageResult = configService.getConfigPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbConfig, pageResult.getList().get(0));
    }

    @Test
    public void testGetConfigList() {
        // mock 数据
        InfConfigDO dbConfig = randomInfConfigDO(o -> { // 等会查询到
            o.setName("芋艿");
            o.setKey("yunai");
            o.setType(InfConfigTypeEnum.SYSTEM.getType());
            o.setCreateTime(buildTime(2021, 2, 1));
        });
        configMapper.insert(dbConfig);
        // 测试 name 不匹配
        configMapper.insert(ObjectUtils.clone(dbConfig, o -> o.setName("土豆")));
        // 测试 key 不匹配
        configMapper.insert(ObjectUtils.clone(dbConfig, o -> o.setKey("tudou")));
        // 测试 type 不匹配
        configMapper.insert(ObjectUtils.clone(dbConfig, o -> o.setType(InfConfigTypeEnum.CUSTOM.getType())));
        // 测试 createTime 不匹配
        configMapper.insert(ObjectUtils.clone(dbConfig, o -> o.setCreateTime(buildTime(2021, 1, 1))));
        // 准备参数
        InfConfigExportReqVO reqVO = new InfConfigExportReqVO();
        reqVO.setName("艿");
        reqVO.setKey("nai");
        reqVO.setType(InfConfigTypeEnum.SYSTEM.getType());
        reqVO.setBeginTime(buildTime(2021, 1, 15));
        reqVO.setEndTime(buildTime(2021, 2, 15));

        // 调用
        List<InfConfigDO> list = configService.getConfigList(reqVO);
        // 断言
        assertEquals(1, list.size());
        assertPojoEquals(dbConfig, list.get(0));
    }

    @Test
    public void testGetConfigByKey() {
        // mock 数据
        InfConfigDO dbConfig = randomInfConfigDO();
        configMapper.insert(dbConfig);// @Sql: 先插入出一条存在的数据
        // 准备参数
        String key = dbConfig.getKey();

        // 调用
        InfConfigDO config = configService.getConfigByKey(key);
        // 断言
        assertNotNull(config);
        assertPojoEquals(dbConfig, config);
    }

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
        assertEquals(InfConfigTypeEnum.CUSTOM.getType(), config.getType());
        // 校验调用
        verify(configProducer, times(1)).sendConfigRefreshMessage();
    }

    @Test
    public void testCreateConfig_keyDuplicate() {
        // 准备参数
        InfConfigCreateReqVO reqVO = randomPojo(InfConfigCreateReqVO.class);
        // mock 数据
        configMapper.insert(randomInfConfigDO(o -> { // @Sql
            o.setKey(reqVO.getKey()); // 模拟 key 重复
        }));

        // 调用, 并断言异常
        assertServiceException(() -> configService.createConfig(reqVO), CONFIG_KEY_DUPLICATE);
    }

    @Test
    public void testUpdateConfig_success() {
        // mock 数据
        InfConfigDO dbConfig = randomInfConfigDO();
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
        // 校验调用
        verify(configProducer, times(1)).sendConfigRefreshMessage();
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
        InfConfigDO dbConfig = randomInfConfigDO(o -> {
            o.setType(InfConfigTypeEnum.CUSTOM.getType()); // 只能删除 CUSTOM 类型
        });
        configMapper.insert(dbConfig);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbConfig.getId();

        // 调用
        configService.deleteConfig(id);
        // 校验数据不存在了
        assertNull(configMapper.selectById(id));
        // 校验调用
        verify(configProducer, times(1)).sendConfigRefreshMessage();
    }

    @Test
    public void testDeleteConfig_canNotDeleteSystemType() {
        // mock 数据
        InfConfigDO dbConfig = randomInfConfigDO(o -> {
            o.setType(InfConfigTypeEnum.SYSTEM.getType()); // SYSTEM 不允许删除
        });
        configMapper.insert(dbConfig);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbConfig.getId();

        // 调用, 并断言异常
        assertServiceException(() -> configService.deleteConfig(id), CONFIG_CAN_NOT_DELETE_SYSTEM_TYPE);
    }

    @Test
    public void testDeleteConfig_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> configService.deleteConfig(id), CONFIG_NOT_EXISTS);
    }

    // ========== 随机对象 ==========

    @SafeVarargs
    private static InfConfigDO randomInfConfigDO(Consumer<InfConfigDO>... consumers) {
        Consumer<InfConfigDO> consumer = (o) -> {
            o.setType(randomEle(InfConfigTypeEnum.values()).getType()); // 保证 key 的范围
        };
        return randomPojo(InfConfigDO.class, ArrayUtils.append(consumer, consumers));
    }

}
