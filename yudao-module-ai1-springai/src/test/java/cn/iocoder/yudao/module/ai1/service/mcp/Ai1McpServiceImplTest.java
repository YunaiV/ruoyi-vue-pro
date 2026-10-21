package cn.iocoder.yudao.module.ai1.service.mcp;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.ai1.controller.admin.mcp.vo.Ai1McpSaveReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.mcp.Ai1McpDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.mcp.Ai1McpMapper;
import cn.iocoder.yudao.module.ai1.enums.mcp.Ai1McpTransportEnum;
import cn.iocoder.yudao.module.ai1.harness.mcp.Ai1McpClientTool;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * {@link Ai1McpServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(Ai1McpServiceImpl.class)
public class Ai1McpServiceImplTest extends BaseDbUnitTest {

    @Resource
    private Ai1McpServiceImpl mcpService;

    @Resource
    private Ai1McpMapper mcpMapper;

    @MockitoBean
    private Ai1McpClientTool mcpClientTool;

    @Test
    public void testCreateMcp_httpGenerateConfig() {
        // 准备参数
        Ai1McpSaveReqVO reqVO = buildMcpSaveReqVO(Ai1McpTransportEnum.HTTP.getTransport())
                .setUrl("http://127.0.0.1:8081/mcp").setHeaders(Collections.singletonMap("Authorization", "Bearer x"));

        // 调用
        Long id = mcpService.createMcp(reqVO);
        // 断言
        Ai1McpDO mcp = mcpMapper.selectById(id);
        assertEquals("Bearer x", mcp.getHeaders().get("Authorization"));
        Map<String, Object> config = JsonUtils.parseMap(mcp.getConfig());
        assertEquals("http", config.get("transport"));
        assertEquals("http://127.0.0.1:8081/mcp", config.get("url"));
        assertEquals("Bearer x", ((Map<?, ?>) config.get("headers")).get("Authorization"));
    }

    @Test
    public void testCreateMcp_transportColumnWins() {
        // 准备参数
        Ai1McpSaveReqVO reqVO = buildMcpSaveReqVO(Ai1McpTransportEnum.STDIO.getTransport()).setUrl("http://ignored")
                .setConfig("{\"transport\":\"http\",\"command\":\"npx\",\"args\":[\"-y\",\"server\"]}");

        // 调用
        Long id = mcpService.createMcp(reqVO);
        // 断言
        Ai1McpDO mcp = mcpMapper.selectById(id);
        assertEquals("stdio", mcp.getTransport());
        assertNull(mcp.getUrl());
        assertEquals("stdio", JsonUtils.parseMap(mcp.getConfig()).get("transport"));
    }

    @Test
    public void testCreateMcp_stdioWithoutCommand() {
        // 准备参数
        Ai1McpSaveReqVO reqVO = buildMcpSaveReqVO(Ai1McpTransportEnum.STDIO.getTransport()).setConfig("{\"args\":[]}");

        // 调用，并断言异常
        assertServiceException(() -> mcpService.createMcp(reqVO), MCP_COMMAND_REQUIRED);
    }

    @Test
    public void testCreateMcp_httpWithoutUrl() {
        // 准备参数
        Ai1McpSaveReqVO reqVO = buildMcpSaveReqVO(Ai1McpTransportEnum.HTTP.getTransport());

        // 调用，并断言异常
        assertServiceException(() -> mcpService.createMcp(reqVO), MCP_URL_REQUIRED);
    }

    @Test
    public void testCreateMcp_configInvalid() {
        // 准备参数
        Ai1McpSaveReqVO reqVO = buildMcpSaveReqVO(Ai1McpTransportEnum.HTTP.getTransport()).setConfig("[1,2]");

        // 调用，并断言异常
        assertServiceException(() -> mcpService.createMcp(reqVO), MCP_CONFIG_INVALID);
    }

    @Test
    public void testUpdateMcp_switchToStdioClearsUrl() {
        // mock 数据
        Long id = mcpService.createMcp(buildMcpSaveReqVO(Ai1McpTransportEnum.HTTP.getTransport()).setUrl("http://127.0.0.1:8081/mcp"));
        // 准备参数
        Ai1McpSaveReqVO reqVO = buildMcpSaveReqVO(Ai1McpTransportEnum.STDIO.getTransport()).setId(id)
                .setConfig("{\"command\":\"uvx\"}");

        // 调用
        mcpService.updateMcp(reqVO);
        // 断言
        Ai1McpDO mcp = mcpMapper.selectById(id);
        assertEquals("stdio", mcp.getTransport());
        assertNull(mcp.getUrl());
        verify(mcpClientTool).evict(id);
    }

    @Test
    public void testUpdateMcp_httpEvictCache() {
        // mock 数据
        Long id = mcpService.createMcp(buildMcpSaveReqVO(Ai1McpTransportEnum.HTTP.getTransport())
                .setUrl("http://127.0.0.1:8081/mcp"));
        // 准备参数
        Ai1McpSaveReqVO reqVO = buildMcpSaveReqVO(Ai1McpTransportEnum.HTTP.getTransport()).setId(id)
                .setUrl("http://127.0.0.1:8082/mcp").setHeaders(Map.of("Authorization", "Bearer new"))
                .setConfig("{\"url\":\"http://127.0.0.1:8081/mcp\",\"headers\":{\"Authorization\":\"Bearer old\"}}");

        // 调用
        mcpService.updateMcp(reqVO);
        // 断言
        Ai1McpDO mcp = mcpService.getMcp(id);
        assertEquals(reqVO.getUrl(), mcp.getUrl());
        assertEquals(reqVO.getHeaders(), mcp.getHeaders());
        Map<String, Object> config = JsonUtils.parseMap(mcp.getConfig());
        assertEquals(reqVO.getUrl(), config.get("url"));
        assertEquals(reqVO.getHeaders(), config.get("headers"));
        verify(mcpClientTool).evict(id);
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("emptyHeaders")
    public void testUpdateMcp_clearHeaders(Map<String, String> headers) {
        // mock 数据
        Long id = mcpService.createMcp(buildMcpSaveReqVO(Ai1McpTransportEnum.HTTP.getTransport())
                .setUrl("http://127.0.0.1:8081/mcp").setHeaders(Map.of("Authorization", "Bearer old")));
        // 准备参数
        Ai1McpSaveReqVO reqVO = buildMcpSaveReqVO(Ai1McpTransportEnum.HTTP.getTransport()).setId(id)
                .setUrl("http://127.0.0.1:8081/mcp").setHeaders(headers)
                .setConfig("{\"transport\":\"http\",\"url\":\"http://127.0.0.1:8081/mcp\","
                        + "\"headers\":{\"Authorization\":\"Bearer old\"}}");

        // 调用
        mcpService.updateMcp(reqVO);
        // 断言
        Ai1McpDO mcp = mcpService.getMcp(id);
        assertTrue(mcp.getHeaders().isEmpty());
        assertFalse(JsonUtils.parseMap(mcp.getConfig()).containsKey("headers"));
        verify(mcpClientTool).evict(id);
    }

    @Test
    public void testCreateMcp_headersColumnWins() {
        // 准备参数
        Ai1McpSaveReqVO reqVO = buildMcpSaveReqVO(Ai1McpTransportEnum.HTTP.getTransport())
                .setUrl("http://127.0.0.1:8081/mcp").setHeaders(Map.of("Authorization", "Bearer column"))
                .setConfig("{\"headers\":{\"Authorization\":\"Bearer config\"}}");

        // 调用
        Long id = mcpService.createMcp(reqVO);
        // 断言平铺请求头覆盖 config 中的旧请求头
        Ai1McpDO mcp = mcpService.getMcp(id);
        assertEquals(reqVO.getHeaders(), mcp.getHeaders());
        assertEquals(mcp.getHeaders(), JsonUtils.parseMap(mcp.getConfig()).get("headers"));
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("emptyHeaders")
    public void testCreateMcp_emptyHeadersRemoveConfigHeaders(Map<String, String> headers) {
        // 准备参数：平铺请求头为空，config 中仍有旧鉴权
        Ai1McpSaveReqVO reqVO = buildMcpSaveReqVO(Ai1McpTransportEnum.HTTP.getTransport())
                .setUrl("http://127.0.0.1:8081/mcp").setHeaders(headers)
                .setConfig("{\"headers\":{\"Authorization\":\"Bearer old\"}}");

        // 调用
        Long id = mcpService.createMcp(reqVO);
        // 断言列表回显和运行时配置均不保留旧请求头
        Ai1McpDO mcp = mcpService.getMcp(id);
        assertTrue(mcp.getHeaders().isEmpty());
        assertFalse(JsonUtils.parseMap(mcp.getConfig()).containsKey("headers"));
    }

    @Test
    public void testCreateMcp_configUrlFallback() {
        // 准备参数
        Ai1McpSaveReqVO reqVO = buildMcpSaveReqVO(Ai1McpTransportEnum.HTTP.getTransport())
                .setConfig("{\"url\":\"http://127.0.0.1:8081/mcp\"}");

        // 调用
        Long id = mcpService.createMcp(reqVO);
        // 断言
        Ai1McpDO mcp = mcpService.getMcp(id);
        assertEquals("http://127.0.0.1:8081/mcp", mcp.getUrl());
        assertEquals(mcp.getUrl(), JsonUtils.parseMap(mcp.getConfig()).get("url"));
    }

    @Test
    public void testCreateMcp_conflictingUrls() {
        // 准备参数
        Ai1McpSaveReqVO reqVO = buildMcpSaveReqVO(Ai1McpTransportEnum.HTTP.getTransport())
                .setUrl("http://127.0.0.1:8081/mcp")
                .setConfig("{\"url\":\"http://127.0.0.1:8082/mcp\"}");

        // 调用
        Long id = mcpService.createMcp(reqVO);
        // 断言平铺地址覆盖 config 中的旧地址
        Ai1McpDO mcp = mcpService.getMcp(id);
        assertEquals(reqVO.getUrl(), mcp.getUrl());
        assertEquals(reqVO.getUrl(), JsonUtils.parseMap(mcp.getConfig()).get("url"));
    }

    @Test
    public void testDeleteMcp_success() {
        // mock 数据
        Long id = mcpService.createMcp(buildMcpSaveReqVO(Ai1McpTransportEnum.STDIO.getTransport())
                .setConfig("{\"command\":\"npx\"}"));

        // 调用
        mcpService.deleteMcp(id);
        // 断言
        assertNull(mcpMapper.selectById(id));
        verify(mcpClientTool).evict(id);
    }

    @Test
    public void testDeleteMcpListByIds_success() {
        // mock 数据
        Long id1 = mcpService.createMcp(buildMcpSaveReqVO(Ai1McpTransportEnum.HTTP.getTransport())
                .setUrl("http://127.0.0.1:8081/mcp"));
        Long id2 = mcpService.createMcp(buildMcpSaveReqVO(Ai1McpTransportEnum.STDIO.getTransport())
                .setConfig("{\"command\":\"npx\"}"));
        Long otherId = mcpService.createMcp(buildMcpSaveReqVO(Ai1McpTransportEnum.HTTP.getTransport())
                .setUrl("http://127.0.0.1:8082/mcp"));
        // 准备参数
        List<Long> ids = List.of(id1, id2);

        // 调用
        mcpService.deleteMcpListByIds(ids);
        // 断言
        assertNull(mcpMapper.selectById(id1));
        assertNull(mcpMapper.selectById(id2));
        assertNotNull(mcpMapper.selectById(otherId));
        verify(mcpClientTool).evict(id1);
        verify(mcpClientTool).evict(id2);
    }

    @Test
    public void testDeleteMcpListByIds_notExists() {
        // mock 数据
        Long id = mcpService.createMcp(buildMcpSaveReqVO(Ai1McpTransportEnum.HTTP.getTransport())
                .setUrl("http://127.0.0.1:8081/mcp"));
        // 准备参数
        List<Long> ids = List.of(id, Long.MAX_VALUE);

        // 调用，并断言异常
        assertServiceException(() -> mcpService.deleteMcpListByIds(ids), MCP_NOT_EXISTS);
        // 断言
        assertNotNull(mcpMapper.selectById(id));
        verifyNoInteractions(mcpClientTool);
    }

    // ========== 测试数据 ==========

    private static Stream<Map<String, String>> emptyHeaders() {
        return Stream.of(Collections.emptyMap());
    }

    private static Ai1McpSaveReqVO buildMcpSaveReqVO(String transport) {
        return new Ai1McpSaveReqVO().setName("天气查询").setTransport(transport).setStatus(CommonStatusEnum.ENABLE.getStatus());
    }

}
