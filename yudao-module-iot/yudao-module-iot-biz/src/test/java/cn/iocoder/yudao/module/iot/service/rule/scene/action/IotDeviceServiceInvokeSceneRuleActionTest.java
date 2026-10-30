package cn.iocoder.yudao.module.iot.service.rule.scene.action;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.message.IotDeviceMessageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

/**
 * {@link IotDeviceServiceInvokeSceneRuleAction} 的单元测试
 *
 * 说明：用例只标记“存在实际事务”，验证 catch 的判断与原语义；不是真实的数据库事务，真实的回滚由 IotDeviceServiceImplTest 覆盖
 *
 * @author 芋道源码
 */
public class IotDeviceServiceInvokeSceneRuleActionTest extends BaseMockitoUnitTest {

    @InjectMocks
    private IotDeviceServiceInvokeSceneRuleAction action;

    @Mock
    private IotDeviceService deviceService;
    @Mock
    private IotDeviceMessageService deviceMessageService;

    @Test
    public void testExecute_sendDataAccessException() {
        // 准备参数
        IotDeviceDO device = IotDeviceDO.builder().id(randomLongId()).build();
        IotSceneRuleDO rule = new IotSceneRuleDO().setId(randomLongId());
        IotSceneRuleDO.Action actionConfig = buildActionConfig(device.getId());
        // mock 方法：发送时读取 serverId 的 Redis 故障
        when(deviceService.getDeviceFromCache(device.getId())).thenReturn(device);
        when(deviceMessageService.sendDeviceMessage(argThat((IotDeviceMessage m) ->
                IotDeviceMessageMethodEnum.SERVICE_INVOKE.getMethod().equals(m.getMethod())), same(device)))
                .thenThrow(new QueryTimeoutException("redis failed"));

        // 调用，并断言异常：状态事务内，不吞掉
        assertThrows(QueryTimeoutException.class,
                () -> executeInActualTransaction(() -> action.execute(null, rule, actionConfig)));
        // 调用：非事务内，与原逻辑一致，记录日志后结束
        assertDoesNotThrow(() -> action.execute(null, rule, actionConfig));
    }

    // ========== 辅助方法 ==========

    /**
     * 标记“存在实际事务”后执行：只用于验证 catch 的判断条件，不是真实的数据库事务
     */
    private static void executeInActualTransaction(Executable executable) throws Throwable {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            executable.execute();
        } finally {
            TransactionSynchronizationManager.setActualTransactionActive(false);
        }
    }

    private static IotSceneRuleDO.Action buildActionConfig(Long deviceId) {
        return new IotSceneRuleDO.Action().setDeviceId(deviceId).setIdentifier("reboot").setParams("{}");
    }

}
