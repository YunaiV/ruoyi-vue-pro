package cn.iocoder.yudao.module.ai1.service.mcp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai1.controller.admin.mcp.vo.Ai1McpConnectRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.mcp.vo.Ai1McpPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.mcp.vo.Ai1McpSaveReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.mcp.Ai1McpDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * AI1 MCP 服务 Service 接口
 *
 * @author 芋道源码
 */
public interface Ai1McpService {

    /**
     * 创建 MCP
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMcp(@Valid Ai1McpSaveReqVO createReqVO);

    /**
     * 更新 MCP
     *
     * @param updateReqVO 更新信息
     */
    void updateMcp(@Valid Ai1McpSaveReqVO updateReqVO);

    /**
     * 删除 MCP
     *
     * @param id 编号
     */
    void deleteMcp(Long id);

    /**
     * 批量删除 MCP
     *
     * @param ids 编号列表
     */
    void deleteMcpListByIds(List<Long> ids);

    /**
     * 获得 MCP
     *
     * @param id 编号
     * @return MCP
     */
    Ai1McpDO getMcp(Long id);

    /**
     * 获得 MCP 分页
     *
     * @param pageReqVO 分页查询
     * @return MCP 分页
     */
    PageResult<Ai1McpDO> getMcpPage(Ai1McpPageReqVO pageReqVO);

    /**
     * 获得指定状态的 MCP 列表
     *
     * @param status 状态
     * @return MCP 列表
     */
    List<Ai1McpDO> getMcpListByStatus(Integer status);

    /**
     * 获得 MCP 列表
     *
     * @param ids 编号集合
     * @return MCP 列表
     */
    List<Ai1McpDO> getMcpList(Collection<Long> ids);

    /**
     * 测试 MCP 连通性
     *
     * @param id 编号
     * @return 测试结果
     */
    Ai1McpConnectRespVO testMcpConnect(Long id);

}
