package cn.iocoder.yudao.module.iot.service.plugininstance;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import jakarta.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.iot.controller.admin.plugininstance.vo.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugininstance.PluginInstanceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.plugininstance.PluginInstanceMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link PluginInstanceServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(PluginInstanceServiceImpl.class)
public class PluginInstanceServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PluginInstanceServiceImpl pluginInstanceService;

    @Resource
    private PluginInstanceMapper pluginInstanceMapper;

    @Test
    public void testCreatePluginInstance_success() {
        // 准备参数
        PluginInstanceSaveReqVO createReqVO = randomPojo(PluginInstanceSaveReqVO.class).setId(null);

        // 调用
        Long pluginInstanceId = pluginInstanceService.createPluginInstance(createReqVO);
        // 断言
        assertNotNull(pluginInstanceId);
        // 校验记录的属性是否正确
        PluginInstanceDO pluginInstance = pluginInstanceMapper.selectById(pluginInstanceId);
        assertPojoEquals(createReqVO, pluginInstance, "id");
    }

    @Test
    public void testUpdatePluginInstance_success() {
        // mock 数据
        PluginInstanceDO dbPluginInstance = randomPojo(PluginInstanceDO.class);
        pluginInstanceMapper.insert(dbPluginInstance);// @Sql: 先插入出一条存在的数据
        // 准备参数
        PluginInstanceSaveReqVO updateReqVO = randomPojo(PluginInstanceSaveReqVO.class, o -> {
            o.setId(dbPluginInstance.getId()); // 设置更新的 ID
        });

        // 调用
        pluginInstanceService.updatePluginInstance(updateReqVO);
        // 校验是否更新正确
        PluginInstanceDO pluginInstance = pluginInstanceMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, pluginInstance);
    }

    @Test
    public void testUpdatePluginInstance_notExists() {
        // 准备参数
        PluginInstanceSaveReqVO updateReqVO = randomPojo(PluginInstanceSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> pluginInstanceService.updatePluginInstance(updateReqVO), PLUGIN_INSTANCE_NOT_EXISTS);
    }

    @Test
    public void testDeletePluginInstance_success() {
        // mock 数据
        PluginInstanceDO dbPluginInstance = randomPojo(PluginInstanceDO.class);
        pluginInstanceMapper.insert(dbPluginInstance);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbPluginInstance.getId();

        // 调用
        pluginInstanceService.deletePluginInstance(id);
       // 校验数据不存在了
       assertNull(pluginInstanceMapper.selectById(id));
    }

    @Test
    public void testDeletePluginInstance_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> pluginInstanceService.deletePluginInstance(id), PLUGIN_INSTANCE_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetPluginInstancePage() {
       // mock 数据
       PluginInstanceDO dbPluginInstance = randomPojo(PluginInstanceDO.class, o -> { // 等会查询到
           o.setMainId(null);
           o.setPluginId(null);
           o.setIp(null);
           o.setPort(null);
           o.setHeartbeatAt(null);
           o.setCreateTime(null);
       });
       pluginInstanceMapper.insert(dbPluginInstance);
       // 测试 mainId 不匹配
       pluginInstanceMapper.insert(cloneIgnoreId(dbPluginInstance, o -> o.setMainId(null)));
       // 测试 pluginId 不匹配
       pluginInstanceMapper.insert(cloneIgnoreId(dbPluginInstance, o -> o.setPluginId(null)));
       // 测试 ip 不匹配
       pluginInstanceMapper.insert(cloneIgnoreId(dbPluginInstance, o -> o.setIp(null)));
       // 测试 port 不匹配
       pluginInstanceMapper.insert(cloneIgnoreId(dbPluginInstance, o -> o.setPort(null)));
       // 测试 heartbeatAt 不匹配
       pluginInstanceMapper.insert(cloneIgnoreId(dbPluginInstance, o -> o.setHeartbeatAt(null)));
       // 测试 createTime 不匹配
       pluginInstanceMapper.insert(cloneIgnoreId(dbPluginInstance, o -> o.setCreateTime(null)));
       // 准备参数
       PluginInstancePageReqVO reqVO = new PluginInstancePageReqVO();
       reqVO.setMainId(null);
       reqVO.setPluginId(null);
       reqVO.setIp(null);
       reqVO.setPort(null);
       reqVO.setHeartbeatAt(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<PluginInstanceDO> pageResult = pluginInstanceService.getPluginInstancePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbPluginInstance, pageResult.getList().get(0));
    }

}