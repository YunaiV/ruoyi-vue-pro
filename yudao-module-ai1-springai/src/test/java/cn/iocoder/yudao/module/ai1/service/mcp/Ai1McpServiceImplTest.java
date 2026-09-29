package cn.iocoder.yudao.module.ai1.service.mcp;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.ai1.controller.admin.mcp.vo.Ai1McpSaveReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.mcp.Ai1McpDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.mcp.Ai1McpMapper;
import cn.iocoder.yudao.module.ai1.enums.mcp.Ai1McpTransportEnum;
import cn.iocoder.yudao.module.ai1.tool.mcp.Ai1McpClientTool;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

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
        // 准备参数：远程，未提供 config
        Ai1McpSaveReqVO reqVO = buildMcpSaveReqVO(Ai1McpTransportEnum.HTTP.getTransport())
                .setUrl("http://127.0.0.1:8081/mcp").setHeaders(Collections.singletonMap("Authorization", "Bearer x"));

        // 调用
        Long id = mcpService.createMcp(reqVO);

        // 断言：按平铺列生成 config，请求头按 JSON 保存
        Ai1McpDO mcp = mcpMapper.selectById(id);
        assertEquals("Bearer x", mcp.getHeaders().get("Authorization"));
        Map<String, Object> config = JsonUtils.parseMap(mcp.getConfig());
        assertEquals("http", config.get("transport"));
        assertEquals("http://127.0.0.1:8081/mcp", config.get("url"));
        assertEquals("Bearer x", ((Map<?, ?>) config.get("headers")).get("Authorization"));
    }

    @Test
    public void testCreateMcp_transportColumnWins() {
        // 准备参数：config 写的是 http，列为 stdio，以列为准
        Ai1McpSaveReqVO reqVO = buildMcpSaveReqVO(Ai1McpTransportEnum.STDIO.getTransport()).setUrl("http://ignored")
                .setConfig("{\"transport\":\"http\",\"command\":\"npx\",\"args\":[\"-y\",\"server\"]}");

        // 调用
        Long id = mcpService.createMcp(reqVO);

        // 断言：transport 写回为 stdio，本地类型不保留服务地址
        Ai1McpDO mcp = mcpMapper.selectById(id);
        assertEquals("stdio", mcp.getTransport());
        assertNull(mcp.getUrl());
        assertEquals("stdio", JsonUtils.parseMap(mcp.getConfig()).get("transport"));
    }

    @Test
    public void testCreateMcp_stdioWithoutCommand() {
        Ai1McpSaveReqVO reqVO = buildMcpSaveReqVO(Ai1McpTransportEnum.STDIO.getTransport()).setConfig("{\"args\":[]}");
        assertServiceException(() -> mcpService.createMcp(reqVO), MCP_COMMAND_REQUIRED);
    }

    @Test
    public void testCreateMcp_httpWithoutUrl() {
        Ai1McpSaveReqVO reqVO = buildMcpSaveReqVO(Ai1McpTransportEnum.HTTP.getTransport());
        assertServiceException(() -> mcpService.createMcp(reqVO), MCP_URL_REQUIRED);
    }

    @Test
    public void testCreateMcp_configInvalid() {
        Ai1McpSaveReqVO reqVO = buildMcpSaveReqVO(Ai1McpTransportEnum.HTTP.getTransport()).setConfig("[1,2]");
        assertServiceException(() -> mcpService.createMcp(reqVO), MCP_CONFIG_INVALID);
    }

    @Test
    public void testUpdateMcp_switchToStdioClearsUrl() {
        // mock 数据
        Long id = mcpService.createMcp(buildMcpSaveReqVO(Ai1McpTransportEnum.HTTP.getTransport()).setUrl("http://127.0.0.1:8081/mcp"));
        // 准备参数：切换为本地
        Ai1McpSaveReqVO reqVO = buildMcpSaveReqVO(Ai1McpTransportEnum.STDIO.getTransport()).setId(id)
                .setConfig("{\"command\":\"uvx\"}");

        // 调用
        mcpService.updateMcp(reqVO);

        // 断言：服务地址被清空，并释放旧连接
        Ai1McpDO mcp = mcpMapper.selectById(id);
        assertEquals("stdio", mcp.getTransport());
        assertNull(mcp.getUrl());
        verify(mcpClientTool).evict(id);
    }

    // ========== 随机对象 ==========

    private static Ai1McpSaveReqVO buildMcpSaveReqVO(String transport) {
        return new Ai1McpSaveReqVO().setName("天气查询").setTransport(transport).setStatus(CommonStatusEnum.ENABLE.getStatus());
    }

}
