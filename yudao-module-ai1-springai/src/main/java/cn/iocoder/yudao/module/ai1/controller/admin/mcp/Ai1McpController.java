package cn.iocoder.yudao.module.ai1.controller.admin.mcp;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai1.controller.admin.mcp.vo.Ai1McpConnectRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.mcp.vo.Ai1McpPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.mcp.vo.Ai1McpRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.mcp.vo.Ai1McpSaveReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.mcp.Ai1McpDO;
import cn.iocoder.yudao.module.ai1.service.mcp.Ai1McpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - AI1 MCP")
@RestController
@RequestMapping("/ai1/mcp")
@Validated
public class Ai1McpController {

    @Resource
    private Ai1McpService mcpService;

    @PostMapping("/create")
    @Operation(summary = "创建 MCP")
    @PreAuthorize("@ss.hasPermission('ai1:mcp:create')")
    public CommonResult<Long> createMcp(@Valid @RequestBody Ai1McpSaveReqVO createReqVO) {
        return success(mcpService.createMcp(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新 MCP")
    @PreAuthorize("@ss.hasPermission('ai1:mcp:update')")
    public CommonResult<Boolean> updateMcp(@Valid @RequestBody Ai1McpSaveReqVO updateReqVO) {
        mcpService.updateMcp(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除 MCP")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('ai1:mcp:delete')")
    public CommonResult<Boolean> deleteMcp(@RequestParam("id") Long id) {
        mcpService.deleteMcp(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除 MCP")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('ai1:mcp:delete')")
    public CommonResult<Boolean> deleteMcpList(@RequestParam("ids") List<Long> ids) {
        mcpService.deleteMcpListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得 MCP")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('ai1:mcp:query')")
    public CommonResult<Ai1McpRespVO> getMcp(@RequestParam("id") Long id) {
        Ai1McpDO mcp = mcpService.getMcp(id);
        return success(BeanUtils.toBean(mcp, Ai1McpRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得 MCP 分页")
    @PreAuthorize("@ss.hasPermission('ai1:mcp:query')")
    public CommonResult<PageResult<Ai1McpRespVO>> getMcpPage(@Valid Ai1McpPageReqVO pageReqVO) {
        PageResult<Ai1McpDO> pageResult = mcpService.getMcpPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, Ai1McpRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得 MCP 精简列表", description = "只包含开启状态，用于 Agent 绑定 MCP 的下拉选择")
    public CommonResult<List<Ai1McpRespVO>> getMcpSimpleList() {
        List<Ai1McpDO> list = mcpService.getMcpListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(list, mcp -> new Ai1McpRespVO()
                .setId(mcp.getId()).setName(mcp.getName()).setTransport(mcp.getTransport())));
    }

    @PostMapping("/test")
    @Operation(summary = "MCP 连通测试", description = "initialize + tools/list")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('ai1:mcp:test')")
    public CommonResult<Ai1McpConnectRespVO> testMcp(@RequestParam("id") Long id) {
        return success(BeanUtils.toBean(mcpService.testMcp(id), Ai1McpConnectRespVO.class));
    }

}
