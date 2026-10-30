package cn.iocoder.yudao.module.iot.mq.consumer.rule;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.service.rule.data.IotDataRuleService;
import cn.iocoder.yudao.module.iot.service.rule.scene.IotSceneRuleService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * {@link IotSceneRuleMessageSubscriber}、{@link IotDataRuleMessageSubscriber} 的单元测试类
 *
 * 状态消息的规则，改由设备消费者在状态事务内触发；两个规则消费者如果不跳过，规则会重复执行
 *
 * @author 芋道源码
 */
public class IotRuleMessageSubscriberTest extends BaseMockitoUnitTest {

    @InjectMocks
    private IotSceneRuleMessageSubscriber sceneRuleMessageSubscriber;
    @InjectMocks
    private IotDataRuleMessageSubscriber dataRuleMessageSubscriber;

    @Mock
    private IotSceneRuleService sceneRuleService;
    @Mock
    private IotDataRuleService dataRuleService;
    @Mock
    private IotMessageBus messageBus;

    @Test
    public void testOnMessage_stateUpdateSkip() {
        // 准备参数
        IotDeviceMessage message = IotDeviceMessage.buildStateOffline().setDeviceId(randomLongId())
                .setTenantId(randomLongId());

        // 调用
        sceneRuleMessageSubscriber.onMessage(message);
        dataRuleMessageSubscriber.onMessage(message);
        // 断言：跳过，不执行规则
        verifyNoInteractions(sceneRuleService, dataRuleService);
    }

    @Test
    public void testOnMessage_propertyPost() {
        // 准备参数
        IotDeviceMessage message = new IotDeviceMessage().setDeviceId(randomLongId()).setTenantId(randomLongId())
                .setMethod(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod());

        // 调用
        sceneRuleMessageSubscriber.onMessage(message);
        dataRuleMessageSubscriber.onMessage(message);
        // 断言：照常执行规则
        verify(sceneRuleService).executeSceneRuleByDevice(same(message));
        verify(dataRuleService).executeDataRule(same(message));
    }

}
