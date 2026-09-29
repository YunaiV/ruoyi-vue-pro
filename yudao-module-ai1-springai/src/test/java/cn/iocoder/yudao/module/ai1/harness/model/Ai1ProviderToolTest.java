package cn.iocoder.yudao.module.ai1.harness.model;

import cn.iocoder.yudao.module.ai1.controller.admin.model.vo.provider.Ai1ProviderConnectRespVO;
import org.junit.jupiter.api.Test;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.PROVIDER_REMOTE_MODEL_LOAD_FAIL;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link Ai1ProviderTool} 的单元测试
 *
 * @author 芋道源码
 */
public class Ai1ProviderToolTest {

    /**
     * 本机不可达的接口地址：端口 1 无服务监听，连接立即被拒绝，无需外网
     */
    private static final String UNREACHABLE_BASE_URL = "http://127.0.0.1:1/v1";

    private final Ai1ProviderTool providerTool = new Ai1ProviderTool();

    @Test
    public void testTestConnect_unreachable() {
        // 调用
        Ai1ProviderConnectRespVO result = providerTool.testConnect(UNREACHABLE_BASE_URL, null, null);

        // 断言：两次请求均网络异常，合并报告
        assertFalse(result.getConnectable());
        assertEquals(0, result.getHttpCode());
        assertTrue(result.getMessage().startsWith("连接失败："));
        assertNotNull(result.getElapsedMs());
    }

    @Test
    public void testListModels_unreachable() {
        // 调用，并断言异常
        assertServiceException(() -> providerTool.listModels(UNREACHABLE_BASE_URL, null, null), PROVIDER_REMOTE_MODEL_LOAD_FAIL);
    }

}
