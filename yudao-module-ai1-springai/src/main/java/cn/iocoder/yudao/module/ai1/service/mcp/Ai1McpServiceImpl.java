package cn.iocoder.yudao.module.ai1.service.mcp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai1.controller.admin.mcp.vo.Ai1McpConnectRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.mcp.vo.Ai1McpPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.mcp.vo.Ai1McpSaveReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.mcp.Ai1McpDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.mcp.Ai1McpMapper;
import cn.iocoder.yudao.module.ai1.enums.mcp.Ai1McpTransportEnum;
import cn.iocoder.yudao.module.ai1.harness.mcp.Ai1McpClientTool;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.*;

/**
 * AI1 MCP 服务 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class Ai1McpServiceImpl implements Ai1McpService {

    @Resource
    private Ai1McpMapper mcpMapper;

    @Resource
    private Ai1McpClientTool mcpClientTool;

    @Override
    public Long createMcp(Ai1McpSaveReqVO createReqVO) {
        Ai1McpDO mcp = buildMcp(createReqVO);
        mcpMapper.insert(mcp);
        return mcp.getId();
    }

    @Override
    public void updateMcp(Ai1McpSaveReqVO updateReqVO) {
        // 1. 校验存在
        validateMcpExists(updateReqVO.getId());

        // 2. 更新：切换为本地时服务地址为空，由 updateForSave 主动置空
        mcpMapper.updateForSave(buildMcp(updateReqVO));

        // 3. 释放本节点的旧连接，下次使用时按新配置重建
        // TODO DONE @AI：集群情况下；
        // 其他节点：客户端缓存按配置指纹比对，加载到新配置时自动重建并关闭旧连接；闲置连接按访问过期自动释放
        mcpClientTool.evict(updateReqVO.getId());
    }

    @Override
    public void deleteMcp(Long id) {
        deleteMcpListByIds(Collections.singletonList(id));
    }

    @Override
    public void deleteMcpListByIds(List<Long> ids) {
        // 1. 校验存在
        ids.forEach(this::validateMcpExists);

        // 2. 删除
        mcpMapper.deleteByIds(ids);

        // 3. 释放本节点的连接与子进程；其他节点的闲置连接按访问过期自动释放
        ids.forEach(mcpClientTool::evict);
    }

    @Override
    public Ai1McpDO getMcp(Long id) {
        return mcpMapper.selectById(id);
    }

    @Override
    public PageResult<Ai1McpDO> getMcpPage(Ai1McpPageReqVO pageReqVO) {
        return mcpMapper.selectPage(pageReqVO);
    }

    @Override
    public List<Ai1McpDO> getMcpListByStatus(Integer status) {
        return mcpMapper.selectListByStatus(status);
    }

    @Override
    public List<Ai1McpDO> getMcpList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return mcpMapper.selectByIds(ids);
    }

    @Override
    public Ai1McpConnectRespVO testMcpConnect(Long id) {
        Ai1McpDO mcp = validateMcpExists(id);
        return mcpClientTool.testConnect(mcp);
    }

    private Ai1McpDO validateMcpExists(Long id) {
        Ai1McpDO mcp = mcpMapper.selectById(id);
        if (mcp == null) {
            throw exception(MCP_NOT_EXISTS);
        }
        return mcp;
    }

    /**
     * 校验并归一化 MCP 配置，保证传输方式、config、平铺列一致：
     * 1. 传输方式以 transport 列为准，写回 config 的 transport 字段
     * 2. 远程：必须有服务地址（平铺列或 config.url）；config 缺失时按平铺列生成
     * 3. 本地：config 必须包含 command，服务地址置空
     */
    private Ai1McpDO buildMcp(Ai1McpSaveReqVO reqVO) {
        // 1.1 解析 config
        Map<String, Object> config = new LinkedHashMap<>();
        if (StrUtil.isNotBlank(reqVO.getConfig())) {
            Map<String, Object> configMap = JsonUtils.parseMap(reqVO.getConfig());
            if (configMap == null) {
                throw exception(MCP_CONFIG_INVALID);
            }
            config.putAll(configMap);
        }
        // 1.2 传输方式以列为准
        config.put("transport", reqVO.getTransport());

        // 2. 按传输方式校验必填项
        String url = StrUtil.blankToDefault(reqVO.getUrl(), MapUtil.getStr(config, "url"));
        if (Ai1McpTransportEnum.isStdio(reqVO.getTransport())) {
            if (StrUtil.isBlank(MapUtil.getStr(config, "command"))) {
                throw exception(MCP_COMMAND_REQUIRED);
            }
            url = null;
        } else {
            if (StrUtil.isBlank(url)) {
                throw exception(MCP_URL_REQUIRED);
            }
            config.putIfAbsent("url", url);
            if (MapUtil.isNotEmpty(reqVO.getHeaders())) {
                config.putIfAbsent("headers", reqVO.getHeaders());
            }
        }

        // 3. 构建
        return BeanUtils.toBean(reqVO, Ai1McpDO.class).setUrl(url).setConfig(JsonUtils.toJsonString(config));
    }

}
