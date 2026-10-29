package cn.iocoder.yudao.module.iot.service.rule.data;

import cn.hutool.core.map.MapUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataRuleDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataSinkDO;
import cn.iocoder.yudao.module.iot.dal.mysql.rule.IotDataRuleMapper;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import cn.iocoder.yudao.module.iot.service.rule.data.action.IotDataRuleAction;
import cn.iocoder.yudao.module.iot.service.thingmodel.IotThingModelService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.context.annotation.Import;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link IotDataRuleServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(IotDataRuleServiceImpl.class)
class IotDataRuleServiceImplTest extends BaseDbUnitTest {

    @Resource
    private IotDataRuleServiceImpl dataRuleService;

    @Resource
    private IotDataRuleMapper dataRuleMapper;
    @Resource
    private PlatformTransactionManager transactionManager;

    @MockitoBean
    private IotDataSinkService dataSinkService;
    @MockitoBean
    private IotDataRuleAction dataRuleAction;
    @MockitoBean
    private IotProductService productService;
    @MockitoBean
    private IotDeviceService deviceService;
    @MockitoBean
    private IotThingModelService thingModelService;

    @Test
    public void testExecuteDataRule_propertyPost_singleIdentifierMatched() {
        // 准备参数
        Long deviceId = randomLongId();
        String identifier = "temperature";
        IotDeviceMessage message = createPropertyPostMessage(deviceId,
                MapUtil.<String, Object>builder().put(identifier, 25.5).build());
        // mock 数据：插入一条限定 identifier=temperature 的规则
        Long sinkId = randomLongId();
        insertEnabledRule(deviceId, IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), identifier, sinkId);
        // mock 方法
        IotDataSinkDO sink = mockEnabledSink(sinkId);

        // 调用
        dataRuleService.executeDataRule(message);

        // 断言：sink action 被调用一次
        verify(dataRuleAction).execute(eq(message), eq(sink));
    }

    @Test
    public void testExecuteDataRule_propertyPost_multiIdentifierOneMatched() {
        // 准备参数：上报 {temperature, humidity}，规则只限定 humidity
        Long deviceId = randomLongId();
        IotDeviceMessage message = createPropertyPostMessage(deviceId,
                MapUtil.<String, Object>builder().put("temperature", 25.5).put("humidity", 60).build());
        // mock 数据
        Long sinkId = randomLongId();
        insertEnabledRule(deviceId, IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), "humidity", sinkId);
        // mock 方法
        IotDataSinkDO sink = mockEnabledSink(sinkId);

        // 调用
        dataRuleService.executeDataRule(message);

        // 断言
        verify(dataRuleAction).execute(eq(message), eq(sink));
    }

    @Test
    public void testExecuteDataRule_propertyPost_multiIdentifierDeduped() {
        // 准备参数：上报 {temperature, humidity}，规则 identifier=null 不限定属性
        Long deviceId = randomLongId();
        IotDeviceMessage message = createPropertyPostMessage(deviceId,
                MapUtil.<String, Object>builder().put("temperature", 25.5).put("humidity", 60).build());
        // mock 数据：identifier=null 时两个属性 key 都会命中同一条规则，需在 sink 调用前去重
        Long sinkId = randomLongId();
        insertEnabledRule(deviceId, IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), null, sinkId);
        // mock 方法
        IotDataSinkDO sink = mockEnabledSink(sinkId);

        // 调用
        dataRuleService.executeDataRule(message);

        // 断言：去重后只触发一次，而不是 2 次
        verify(dataRuleAction).execute(eq(message), eq(sink));
    }

    @Test
    public void testExecuteDataRule_propertyPost_multiRuleSameSinkDeduped() {
        // 准备参数：上报 {temperature, humidity}，两条规则分别命中不同 identifier，但都指向同一 sink
        Long deviceId = randomLongId();
        IotDeviceMessage message = createPropertyPostMessage(deviceId,
                MapUtil.<String, Object>builder().put("temperature", 25.5).put("humidity", 60).build());
        // mock 数据：插入两条规则，identifier 分别为 temperature 与 humidity，sinkId 相同
        Long sinkId = randomLongId();
        insertEnabledRule(deviceId, IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), "temperature", sinkId);
        insertEnabledRule(deviceId, IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), "humidity", sinkId);
        // mock 方法
        IotDataSinkDO sink = mockEnabledSink(sinkId);

        // 调用
        dataRuleService.executeDataRule(message);

        // 断言：跨规则去重后，sink action 只触发一次，而不是 2 次
        verify(dataRuleAction).execute(eq(message), eq(sink));
    }

    @Test
    public void testExecuteDataRule_propertyPost_emptyParamsMatchesWildcardRule() {
        // 准备参数：上报空属性，规则 identifier=null 不限定属性，按"任意 property report 都同步"语义应命中
        Long deviceId = randomLongId();
        IotDeviceMessage message = createPropertyPostMessage(deviceId,
                MapUtil.<String, Object>builder().build());
        // mock 数据
        Long sinkId = randomLongId();
        insertEnabledRule(deviceId, IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), null, sinkId);
        // mock 方法
        IotDataSinkDO sink = mockEnabledSink(sinkId);

        // 调用
        dataRuleService.executeDataRule(message);

        // 断言
        verify(dataRuleAction).execute(eq(message), eq(sink));
    }

    @Test
    public void testExecuteDataRule_propertyPost_noIdentifierMatched() {
        // 准备参数：上报 {temperature}，规则限定 humidity
        Long deviceId = randomLongId();
        IotDeviceMessage message = createPropertyPostMessage(deviceId,
                MapUtil.<String, Object>builder().put("temperature", 25.5).build());
        // mock 数据
        Long sinkId = randomLongId();
        insertEnabledRule(deviceId, IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), "humidity", sinkId);

        // 调用
        dataRuleService.executeDataRule(message);

        // 断言：sink action 不应被调用
        verify(dataRuleAction, never()).execute(any(), any());
    }

    @Test
    public void testExecuteDataRule_eventPost_singleIdentifierMatched() {
        // 准备参数：事件触发器走单 identifier 路径（与改动前行为保持一致）
        Long deviceId = randomLongId();
        String identifier = "alarm";
        IotDeviceMessage message = randomPojo(IotDeviceMessage.class, o -> {
            o.setDeviceId(deviceId);
            o.setMethod(IotDeviceMessageMethodEnum.EVENT_POST.getMethod());
            o.setParams(MapUtil.<String, Object>builder()
                    .put("identifier", identifier).put("value", "fired").build());
        });
        // mock 数据
        Long sinkId = randomLongId();
        insertEnabledRule(deviceId, IotDeviceMessageMethodEnum.EVENT_POST.getMethod(), identifier, sinkId);
        // mock 方法
        IotDataSinkDO sink = mockEnabledSink(sinkId);

        // 调用
        dataRuleService.executeDataRule(message);

        // 断言
        verify(dataRuleAction).execute(eq(message), eq(sink));
    }

    // ========== executeDataRule：状态事务内的数据访问异常 ==========

    @Test
    public void testExecuteDataRule_actionDataAccessException() {
        // 准备参数
        Long deviceId = randomLongId();
        String identifier = "temperature";
        IotDeviceMessage message = createPropertyPostMessage(deviceId,
                MapUtil.<String, Object>builder().put(identifier, 25.5).build());
        // mock 数据
        Long sinkId = randomLongId();
        insertEnabledRule(deviceId, IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), identifier, sinkId);
        // mock 方法：数据目的访问数据失败
        IotDataSinkDO sink = mockEnabledSink(sinkId);
        doThrow(new QueryTimeoutException("data access failed")).when(dataRuleAction).execute(eq(message), eq(sink));

        // 调用，并断言异常：状态事务内，数据访问异常不吞掉，交给事务回滚
        new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                assertThrows(QueryTimeoutException.class, () -> dataRuleService.executeDataRule(message)));
        // 调用：非事务内，与原逻辑一致，记录日志后结束
        assertDoesNotThrow(() -> dataRuleService.executeDataRule(message));
    }

    @Test
    public void testExecuteDataRule_sinkDataAccessException() {
        // 准备参数
        Long deviceId = randomLongId();
        String identifier = "temperature";
        IotDeviceMessage message = createPropertyPostMessage(deviceId,
                MapUtil.<String, Object>builder().put(identifier, 25.5).build());
        // mock 数据：一条规则，两个数据目的
        Long sinkId1 = randomLongId();
        Long sinkId2 = randomLongId();
        insertEnabledRule(deviceId, IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), identifier, sinkId1, sinkId2);
        // mock 方法：读取第一个数据目的失败（例如说 Redis 超时）；第二个数据目的正常
        when(dataSinkService.getDataSinkFromCache(sinkId1)).thenThrow(new QueryTimeoutException("sink query failed"));
        IotDataSinkDO sink2 = mockEnabledSink(sinkId2);

        // 调用，并断言异常：状态事务内，不吞掉，后续数据目的不再执行
        new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                assertThrows(QueryTimeoutException.class, () -> dataRuleService.executeDataRule(message)));
        verify(dataRuleAction, never()).execute(any(), any());
        // 调用：非事务内，与原逻辑一致，记录日志后继续执行第二个数据目的
        assertDoesNotThrow(() -> dataRuleService.executeDataRule(message));
        verify(dataRuleAction, times(1)).execute(eq(message), eq(sink2));
    }

    @Test
    public void testExecuteDataRule_matchDataAccessException() {
        // 准备参数
        IotDeviceMessage message = createPropertyPostMessage(randomLongId(),
                MapUtil.<String, Object>builder().put("temperature", 25.5).build());
        // mock 方法：匹配规则时访问数据失败（getSelf 的缓存方法）
        IotDataRuleServiceImpl self = mock(IotDataRuleServiceImpl.class);
        when(self.getDataRuleListByConditionFromCache(anyLong(), anyString(), any()))
                .thenThrow(new QueryTimeoutException("rule query failed"));

        try (MockedStatic<SpringUtil> springUtilMock = mockStatic(SpringUtil.class)) {
            springUtilMock.when(() -> SpringUtil.getBean(IotDataRuleServiceImpl.class)).thenReturn(self);
            // 调用，并断言异常：状态事务内，不吞掉
            new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                    assertThrows(QueryTimeoutException.class, () -> dataRuleService.executeDataRule(message)));
            // 调用：非事务内，与原逻辑一致，记录日志后结束
            assertDoesNotThrow(() -> dataRuleService.executeDataRule(message));
        }
        verify(dataRuleAction, never()).execute(any(), any());
    }

    @Test
    public void testExecuteDataRule_otherExceptionInTransaction() {
        // 准备参数
        Long deviceId = randomLongId();
        String identifier = "temperature";
        IotDeviceMessage message = createPropertyPostMessage(deviceId,
                MapUtil.<String, Object>builder().put(identifier, 25.5).build());
        // mock 数据
        Long sinkId = randomLongId();
        insertEnabledRule(deviceId, IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), identifier, sinkId);
        // mock 方法：数据目的的其它异常（例如说参数错误）
        IotDataSinkDO sink = mockEnabledSink(sinkId);
        doThrow(new IllegalArgumentException("config invalid")).when(dataRuleAction).execute(eq(message), eq(sink));

        // 调用：与原逻辑一致，记录日志后继续
        new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                assertDoesNotThrow(() -> dataRuleService.executeDataRule(message)));
    }

    // ========== 辅助方法 ==========

    private IotDeviceMessage createPropertyPostMessage(Long deviceId, Map<String, Object> params) {
        return randomPojo(IotDeviceMessage.class, o -> {
            o.setDeviceId(deviceId);
            o.setMethod(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod());
            o.setParams(params);
        });
    }

    /**
     * 向 H2 中插入一条启用状态的数据流转规则，命中后会路由到 {@code sinkIds}
     */
    private void insertEnabledRule(Long deviceId, String method, String identifier, Long... sinkIds) {
        IotDataRuleDO.SourceConfig config = randomPojo(IotDataRuleDO.SourceConfig.class, o -> {
            o.setDeviceId(deviceId);
            o.setMethod(method);
            o.setIdentifier(identifier);
        });
        IotDataRuleDO rule = randomPojo(IotDataRuleDO.class, o -> {
            o.setId(null);
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setSourceConfigs(singletonList(config));
            o.setSinkIds(Arrays.asList(sinkIds));
        });
        dataRuleMapper.insert(rule);
    }

    /**
     * 构造一个启用状态的数据流转目的并塞入对应 mock；返回 sink 用于断言
     */
    private IotDataSinkDO mockEnabledSink(Long sinkId) {
        IotDataSinkDO sink = randomPojo(IotDataSinkDO.class, o -> {
            o.setId(sinkId);
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        when(dataSinkService.getDataSinkFromCache(sinkId)).thenReturn(sink);
        when(dataRuleAction.getType()).thenReturn(sink.getType());
        return sink;
    }

}
