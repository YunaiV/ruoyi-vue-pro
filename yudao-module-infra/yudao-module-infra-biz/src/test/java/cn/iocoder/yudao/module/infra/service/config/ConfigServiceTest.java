package cn.iocoder.yudao.module.infra.service.config;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.framework.test.core.util.RandomUtils;
import cn.iocoder.yudao.module.infra.controller.admin.config.vo.ConfigCreateReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.config.vo.ConfigExportReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.config.vo.ConfigPageReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.config.vo.ConfigUpdateReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.config.ConfigDO;
import cn.iocoder.yudao.module.infra.dal.mysql.config.ConfigMapper;
import cn.iocoder.yudao.module.infra.enums.config.ConfigTypeEnum;
import cn.iocoder.yudao.module.infra.mq.producer.config.ConfigProducer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.buildTime;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Import(ConfigServiceImpl.class)
public class ConfigServiceTest extends BaseDbUnitTest {

    @Resource
    private ConfigServiceImpl configService;

    @Resource
    private ConfigMapper configMapper;
    @MockBean
    private ConfigProducer configProducer;

    @Test
    public void testCreateConfig_success() {
        // 准备参数
        ConfigCreateReqVO reqVO = randomPojo(ConfigCreateReqVO.class);

        // 调用
        Long configId = configService.createConfig(reqVO);
        // 断言
        assertNotNull(configId);
        // 校验记录的属性是否正确
        ConfigDO config = configMapper.selectById(configId);
        assertPojoEquals(reqVO, config);
        Assertions.assertEquals(ConfigTypeEnum.CUSTOM.getType(), config.getType());
        // 校验调用
        verify(configProducer, times(1)).sendConfigRefreshMessage();
    }

    @Test
    public void testUpdateConfig_success() {
        // mock 数据
        ConfigDO dbConfig = randomConfigDO();
        configMapper.insert(dbConfig);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ConfigUpdateReqVO reqVO = randomPojo(ConfigUpdateReqVO.class, o -> {
            o.setId(dbConfig.getId()); // 设置更新的 ID
        });

        // 调用
        configService.updateConfig(reqVO);
        // 校验是否更新正确
        ConfigDO config = configMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, config);
        // 校验调用
        verify(configProducer, times(1)).sendConfigRefreshMessage();
    }

    @Test
    public void testDeleteConfig_success() {
        // mock 数据
        ConfigDO dbConfig = randomConfigDO(o -> {
            o.setType(ConfigTypeEnum.CUSTOM.getType()); // 只能删除 CUSTOM 类型
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
        ConfigDO dbConfig = randomConfigDO(o -> {
            o.setType(ConfigTypeEnum.SYSTEM.getType()); // SYSTEM 不允许删除
        });
        configMapper.insert(dbConfig);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbConfig.getId();

        // 调用, 并断言异常
        assertServiceException(() -> configService.deleteConfig(id), CONFIG_CAN_NOT_DELETE_SYSTEM_TYPE);
    }

    @Test
    public void testCheckConfigExists_success() {
        // mock 数据
        ConfigDO dbConfigDO = randomConfigDO();
        configMapper.insert(dbConfigDO);// @Sql: 先插入出一条存在的数据

        // 调用成功
        configService.checkConfigExists(dbConfigDO.getId());
    }

    @Test
    public void testCheckConfigExist_notExists() {
        assertServiceException(() -> configService.checkConfigExists(randomLongId()), CONFIG_NOT_EXISTS);
    }

    @Test
    public void testCheckConfigKeyUnique_success() {
        // 调用，成功
        configService.checkConfigKeyUnique(randomLongId(), randomString());
    }

    @Test
    public void testCheckConfigKeyUnique_keyDuplicateForCreate() {
        // 准备参数
        String key = randomString();
        // mock 数据
        configMapper.insert(randomConfigDO(o -> o.setConfigKey(key)));

        // 调用，校验异常
        assertServiceException(() -> configService.checkConfigKeyUnique(null, key),
                CONFIG_KEY_DUPLICATE);
    }

    @Test
    public void testCheckConfigKeyUnique_keyDuplicateForUpdate() {
        // 准备参数
        Long id = randomLongId();
        String key = randomString();
        // mock 数据
        configMapper.insert(randomConfigDO(o -> o.setConfigKey(key)));

        // 调用，校验异常
        assertServiceException(() -> configService.checkConfigKeyUnique(id, key),
                CONFIG_KEY_DUPLICATE);
    }

    @Test
    public void testGetConfigPage() {
        // mock 数据
        ConfigDO dbConfig = randomConfigDO(o -> { // 等会查询到
            o.setName("芋艿");
            o.setConfigKey("yunai");
            o.setType(ConfigTypeEnum.SYSTEM.getType());
            o.setCreateTime(buildTime(2021, 2, 1));
        });
        configMapper.insert(dbConfig);
        // 测试 name 不匹配
        configMapper.insert(ObjectUtils.cloneIgnoreId(dbConfig, o -> o.setName("土豆")));
        // 测试 key 不匹配
        configMapper.insert(ObjectUtils.cloneIgnoreId(dbConfig, o -> o.setConfigKey("tudou")));
        // 测试 type 不匹配
        configMapper.insert(ObjectUtils.cloneIgnoreId(dbConfig, o -> o.setType(ConfigTypeEnum.CUSTOM.getType())));
        // 测试 createTime 不匹配
        configMapper.insert(ObjectUtils.cloneIgnoreId(dbConfig, o -> o.setCreateTime(buildTime(2021, 1, 1))));
        // 准备参数
        ConfigPageReqVO reqVO = new ConfigPageReqVO();
        reqVO.setName("艿");
        reqVO.setKey("nai");
        reqVO.setType(ConfigTypeEnum.SYSTEM.getType());
        reqVO.setCreateTime((new Date[]{buildTime(2021, 1, 15),buildTime(2021, 2, 15)}));

        // 调用
        PageResult<ConfigDO> pageResult = configService.getConfigPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbConfig, pageResult.getList().get(0));
    }

    @Test
    public void testGetConfigList() {
        // mock 数据
        ConfigDO dbConfig = randomConfigDO(o -> { // 等会查询到
            o.setName("芋艿");
            o.setConfigKey("yunai");
            o.setType(ConfigTypeEnum.SYSTEM.getType());
            o.setCreateTime(buildTime(2021, 2, 1));
        });
        configMapper.insert(dbConfig);
        // 测试 name 不匹配
        configMapper.insert(ObjectUtils.cloneIgnoreId(dbConfig, o -> o.setName("土豆")));
        // 测试 key 不匹配
        configMapper.insert(ObjectUtils.cloneIgnoreId(dbConfig, o -> o.setConfigKey("tudou")));
        // 测试 type 不匹配
        configMapper.insert(ObjectUtils.cloneIgnoreId(dbConfig, o -> o.setType(ConfigTypeEnum.CUSTOM.getType())));
        // 测试 createTime 不匹配
        configMapper.insert(ObjectUtils.cloneIgnoreId(dbConfig, o -> o.setCreateTime(buildTime(2021, 1, 1))));
        // 准备参数
        ConfigExportReqVO reqVO = new ConfigExportReqVO();
        reqVO.setName("艿");
        reqVO.setKey("nai");
        reqVO.setType(ConfigTypeEnum.SYSTEM.getType());
        reqVO.setCreateTime((new Date[]{buildTime(2021, 1, 15),buildTime(2021, 2, 15)}));

        // 调用
        List<ConfigDO> list = configService.getConfigList(reqVO);
        // 断言
        assertEquals(1, list.size());
        assertPojoEquals(dbConfig, list.get(0));
    }

    @Test
    public void testGetConfigByKey() {
        // mock 数据
        ConfigDO dbConfig = randomConfigDO();
        configMapper.insert(dbConfig);// @Sql: 先插入出一条存在的数据
        // 准备参数
        String key = dbConfig.getConfigKey();

        // 调用
        ConfigDO config = configService.getConfigByKey(key);
        // 断言
        assertNotNull(config);
        assertPojoEquals(dbConfig, config);
    }

    // ========== 随机对象 ==========

    @SafeVarargs
    private static ConfigDO randomConfigDO(Consumer<ConfigDO>... consumers) {
        Consumer<ConfigDO> consumer = (o) -> {
            o.setType(randomEle(ConfigTypeEnum.values()).getType()); // 保证 key 的范围
        };
        return RandomUtils.randomPojo(ConfigDO.class, ArrayUtils.append(consumer, consumers));
    }

}
