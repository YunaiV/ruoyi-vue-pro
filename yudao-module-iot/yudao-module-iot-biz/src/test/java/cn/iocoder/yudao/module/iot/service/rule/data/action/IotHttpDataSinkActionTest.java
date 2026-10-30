package cn.iocoder.yudao.module.iot.service.rule.data.action;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataSinkDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.config.IotDataSinkHttpConfig;
import cn.iocoder.yudao.module.iot.enums.rule.IotDataSinkTypeEnum;
import org.junit.jupiter.api.Test;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.InetAddress;
import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link IotHttpDataSinkAction} 的单元测试
 *
 * @author 芋道源码
 */
public class IotHttpDataSinkActionTest {

    @Test
    public void testExecute_readTimeout() throws Exception {
        // 准备参数：本地只建立连接、不响应的 server（不 accept，由 backlog 完成连接）
        try (ServerSocket serverSocket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            IotHttpDataSinkAction action = new IotHttpDataSinkAction();
            ReflectionTestUtils.setField(action, "restTemplateBuilder", new RestTemplateBuilder());
            action.init();
            IotDataSinkHttpConfig config = new IotDataSinkHttpConfig()
                    .setUrl("http://127.0.0.1:" + serverSocket.getLocalPort() + "/hook")
                    .setMethod(HttpMethod.GET.name());
            IotDataSinkDO dataSink = new IotDataSinkDO().setId(1L).setType(IotDataSinkTypeEnum.HTTP.getType())
                    .setConfig(config);
            IotDeviceMessage message = new IotDeviceMessage().setId("msg-1").setDeviceId(2L).setTenantId(1L);

            // 调用：超时异常由 action 内部记录日志，不抛出
            long start = System.currentTimeMillis();
            action.execute(message, dataSink);
            long cost = System.currentTimeMillis() - start;
            // 断言：按读取超时返回，不会无限等待
            long readTimeout = IotHttpDataSinkAction.READ_TIMEOUT.toMillis();
            assertTrue(cost >= readTimeout - 500, "耗时过短：" + cost);
            assertTrue(cost < readTimeout + IotHttpDataSinkAction.CONNECT_TIMEOUT.toMillis(), "耗时过长：" + cost);
        }
    }

}
