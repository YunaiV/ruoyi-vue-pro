package cn.iocoder.yudao.module.iot.service.rule.data;

import cn.hutool.core.map.MapUtil;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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

    // ========== 辅助方法 ==========

    private IotDeviceMessage createPropertyPostMessage(Long deviceId, Map<String, Object> params) {
        return randomPojo(IotDeviceMessage.class, o -> {
            o.setDeviceId(deviceId);
            o.setMethod(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod());
            o.setParams(params);
        });
    }

    /**
     * 向 H2 中插入一条启用状态的数据流转规则，命中后会路由到 {@code sinkId}
     */
    private void insertEnabledRule(Long deviceId, String method, String identifier, Long sinkId) {
        IotDataRuleDO.SourceConfig config = randomPojo(IotDataRuleDO.SourceConfig.class, o -> {
            o.setDeviceId(deviceId);
            o.setMethod(method);
            o.setIdentifier(identifier);
        });
        IotDataRuleDO rule = randomPojo(IotDataRuleDO.class, o -> {
            o.setId(null);
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setSourceConfigs(singletonList(config));
            o.setSinkIds(singletonList(sinkId));
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
