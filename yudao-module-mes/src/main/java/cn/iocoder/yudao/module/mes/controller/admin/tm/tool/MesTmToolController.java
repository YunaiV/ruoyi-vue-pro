package cn.iocoder.yudao.module.mes.controller.admin.tm.tool;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.tm.tool.vo.MesTmToolPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.tm.tool.vo.MesTmToolRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.tm.tool.vo.MesTmToolSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.tm.tool.MesTmToolDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.tm.tool.MesTmToolTypeDO;
import cn.iocoder.yudao.module.mes.service.tm.tool.MesTmToolService;
import cn.iocoder.yudao.module.mes.service.tm.tool.MesTmToolTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - MES 工具台账")
@RestController
@RequestMapping("/mes/tm/tool")
@Validated
public class MesTmToolController {

    @Resource
    private MesTmToolService toolService;

    @Resource
    private MesTmToolTypeService toolTypeService;

    @PostMapping("/create")
    @Operation(summary = "创建工具")
    @PreAuthorize("@ss.hasPermission('mes:tm-tool:create')")
    public CommonResult<Long> createTool(@Valid @RequestBody MesTmToolSaveReqVO createReqVO) {
        return success(toolService.createTool(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新工具")
    @PreAuthorize("@ss.hasPermission('mes:tm-tool:update')")
    public CommonResult<Boolean> updateTool(@Valid @RequestBody MesTmToolSaveReqVO updateReqVO) {
        toolService.updateTool(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除工具")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:tm-tool:delete')")
    public CommonResult<Boolean> deleteTool(@RequestParam("id") Long id) {
        toolService.deleteTool(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得工具")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:tm-tool:query')")
    public CommonResult<MesTmToolRespVO> getTool(@RequestParam("id") Long id) {
        MesTmToolDO tool = toolService.getTool(id);
        MesTmToolRespVO respVO = BeanUtils.toBean(tool, MesTmToolRespVO.class);
        // 拼接工具类型名称
        if (respVO != null && respVO.getToolTypeId() != null) {
            MesTmToolTypeDO toolType = toolTypeService.getToolType(respVO.getToolTypeId());
            if (toolType != null) {
                respVO.setToolTypeName(toolType.getName());
            }
        }
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得工具分页")
    @PreAuthorize("@ss.hasPermission('mes:tm-tool:query')")
    public CommonResult<PageResult<MesTmToolRespVO>> getToolPage(@Valid MesTmToolPageReqVO pageReqVO) {
        PageResult<MesTmToolDO> pageResult = toolService.getToolPage(pageReqVO);
        return success(new PageResult<>(buildToolRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出工具 Excel")
    @PreAuthorize("@ss.hasPermission('mes:tm-tool:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportToolExcel(@Valid MesTmToolPageReqVO pageReqVO,
                                HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MesTmToolDO> list = toolService.getToolPage(pageReqVO).getList();
        List<MesTmToolRespVO> voList = buildToolRespVOList(list);
        // 导出 Excel
        ExcelUtils.write(response, "工具台账.xls", "数据", MesTmToolRespVO.class, voList);
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得工具精简列表")
    @PreAuthorize("@ss.hasPermission('mes:tm-tool:query')")
    public CommonResult<List<MesTmToolRespVO>> getToolSimpleList() {
        List<MesTmToolDO> list = toolService.getToolList();
        return success(BeanUtils.toBean(list, MesTmToolRespVO.class));
    }

    // ==================== 拼接 VO ====================

    @SuppressWarnings("CodeBlock2Expr")
    private List<MesTmToolRespVO> buildToolRespVOList(List<MesTmToolDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 批量获取工具类型信息
        Map<Long, MesTmToolTypeDO> toolTypeMap = toolTypeService.getToolTypeMap(
                convertSet(list, MesTmToolDO::getToolTypeId));
        // 2. 拼接 VO
        return BeanUtils.toBean(list, MesTmToolRespVO.class, vo -> {
            MapUtils.findAndThen(toolTypeMap, vo.getToolTypeId(),
                    toolType -> vo.setToolTypeName(toolType.getName()));
        });
    }

}
