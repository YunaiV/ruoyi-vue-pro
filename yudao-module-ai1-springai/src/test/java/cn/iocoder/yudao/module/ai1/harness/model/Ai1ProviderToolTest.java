package cn.iocoder.yudao.module.ai1.harness.model;

import cn.iocoder.yudao.module.ai1.controller.admin.model.vo.provider.Ai1ProviderConnectRespVO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.PROVIDER_REMOTE_MODEL_LOAD_FAIL;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link Ai1ProviderTool} 的单元测试
 *
 * @author 芋道源码
 */
public class Ai1ProviderToolTest {

    // 本机不可达的接口地址
    private static final String UNREACHABLE_BASE_URL = "http://127.0.0.1:1/v1";

    private final Ai1ProviderTool providerTool = new Ai1ProviderTool();

    @Test
    public void testTestConnect_unreachable() {
        // 调用
        Ai1ProviderConnectRespVO result = providerTool.testConnect(UNREACHABLE_BASE_URL, null, null);
        // 断言
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

    @Test
    public void testParseModels() {
        // 调用，并断言过滤无效模型
        assertEquals(List.of("gpt-4o", "qwen3"), Ai1ProviderTool.parseModels(
                "{\"data\":[{\"id\":\"gpt-4o\"},{\"id\":\"\"},\"x\",{\"id\":\"qwen3\"}]}"));
        // 调用，并断言空列表
        assertTrue(Ai1ProviderTool.parseModels("{\"object\":\"list\"}").isEmpty());
        assertTrue(Ai1ProviderTool.parseModels("{\"data\":[]}").isEmpty());
        // 调用，并断言解析失败
        assertNull(Ai1ProviderTool.parseModels("{\"data\":{\"id\":\"gpt-4o\"}}"));
        assertNull(Ai1ProviderTool.parseModels("not json"));
    }

}
