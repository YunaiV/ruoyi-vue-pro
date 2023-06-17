package cn.iocoder.yudao.module.point.service.pointconfig;

import cn.iocoder.yudao.framework.test.core.util.RandomUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.point.controller.admin.pointconfig.vo.*;
import cn.iocoder.yudao.module.point.dal.dataobject.pointconfig.PointConfigDO;
import cn.iocoder.yudao.module.point.dal.mysql.pointconfig.PointConfigMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import javax.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static cn.iocoder.yudao.module.point.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link PointConfigServiceImpl} 的单元测试类
 *
 * @author QingX
 */
@Import(PointConfigServiceImpl.class)
public class PointConfigServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PointConfigServiceImpl configService;

    @Resource
    private PointConfigMapper configMapper;

    @Test
    public void testCreateConfig_success() {
        // 准备参数
        PointConfigCreateReqVO reqVO = randomPojo(PointConfigCreateReqVO.class);

        // 调用
        Integer configId = configService.createConfig(reqVO);
        // 断言
        assertNotNull(configId);
        // 校验记录的属性是否正确
        PointConfigDO config = configMapper.selectById(configId);
        assertPojoEquals(reqVO, config);
    }

    @Test
    public void testUpdateConfig_success() {
        // mock 数据
        PointConfigDO dbConfig = randomPojo(PointConfigDO.class);
        configMapper.insert(dbConfig);// @Sql: 先插入出一条存在的数据
        // 准备参数
        PointConfigUpdateReqVO reqVO = randomPojo(PointConfigUpdateReqVO.class, o -> {
            o.setId(dbConfig.getId()); // 设置更新的 ID
        });

        // 调用
        configService.updateConfig(reqVO);
        // 校验是否更新正确
        PointConfigDO config = configMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, config);
    }

    @Test
    public void testUpdateConfig_notExists() {
        // 准备参数
        PointConfigUpdateReqVO reqVO = randomPojo(PointConfigUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> configService.updateConfig(reqVO), CONFIG_NOT_EXISTS);
    }

    @Test
    public void testDeleteConfig_success() {
        // mock 数据
        PointConfigDO dbConfig = randomPojo(PointConfigDO.class);
        configMapper.insert(dbConfig);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Integer id = dbConfig.getId();

        // 调用
        configService.deleteConfig(id);
       // 校验数据不存在了
       assertNull(configMapper.selectById(id));
    }

    @Test
    public void testDeleteConfig_notExists() {
        // 准备参数
        Integer id = RandomUtils.randomInteger();

        // 调用, 并断言异常
        assertServiceException(() -> configService.deleteConfig(id), CONFIG_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetConfigPage() {
       // mock 数据
       PointConfigDO dbConfig = randomPojo(PointConfigDO.class, o -> { // 等会查询到
           o.setTradeDeductEnable(null);
       });
       configMapper.insert(dbConfig);
       // 测试 tradeDeductEnable 不匹配
       configMapper.insert(cloneIgnoreId(dbConfig, o -> o.setTradeDeductEnable(null)));
       // 准备参数
       PointConfigPageReqVO reqVO = new PointConfigPageReqVO();
       reqVO.setTradeDeductEnable(null);

       // 调用
       PageResult<PointConfigDO> pageResult = configService.getConfigPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbConfig, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetConfigList() {
       // mock 数据
       PointConfigDO dbConfig = randomPojo(PointConfigDO.class, o -> { // 等会查询到
           o.setTradeDeductEnable(null);
       });
       configMapper.insert(dbConfig);
       // 测试 tradeDeductEnable 不匹配
       configMapper.insert(cloneIgnoreId(dbConfig, o -> o.setTradeDeductEnable(null)));
       // 准备参数
       PointConfigExportReqVO reqVO = new PointConfigExportReqVO();
       reqVO.setTradeDeductEnable(null);

       // 调用
       List<PointConfigDO> list = configService.getConfigList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbConfig, list.get(0));
    }

}
