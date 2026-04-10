package cn.iocoder.yudao.module.mes.controller.admin.tm.tool;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.tm.tool.vo.type.MesTmToolTypePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.tm.tool.vo.type.MesTmToolTypeRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.tm.tool.vo.type.MesTmToolTypeSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.tm.tool.MesTmToolTypeDO;
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

import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - MES 工具类型")
@RestController
@RequestMapping("/mes/tm/tool-type")
@Validated
public class MesTmToolTypeController {

    @Resource
    private MesTmToolTypeService toolTypeService;

    @PostMapping("/create")
    @Operation(summary = "创建工具类型")
    @PreAuthorize("@ss.hasPermission('mes:tm-tool-type:create')")
    public CommonResult<Long> createToolType(@Valid @RequestBody MesTmToolTypeSaveReqVO createReqVO) {
        return success(toolTypeService.createToolType(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新工具类型")
    @PreAuthorize("@ss.hasPermission('mes:tm-tool-type:update')")
    public CommonResult<Boolean> updateToolType(@Valid @RequestBody MesTmToolTypeSaveReqVO updateReqVO) {
        toolTypeService.updateToolType(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除工具类型")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:tm-tool-type:delete')")
    public CommonResult<Boolean> deleteToolType(@RequestParam("id") Long id) {
        toolTypeService.deleteToolType(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得工具类型")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:tm-tool-type:query')")
    public CommonResult<MesTmToolTypeRespVO> getToolType(@RequestParam("id") Long id) {
        MesTmToolTypeDO toolType = toolTypeService.getToolType(id);
        return success(BeanUtils.toBean(toolType, MesTmToolTypeRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得工具类型分页")
    @PreAuthorize("@ss.hasPermission('mes:tm-tool-type:query')")
    public CommonResult<PageResult<MesTmToolTypeRespVO>> getToolTypePage(@Valid MesTmToolTypePageReqVO pageReqVO) {
        PageResult<MesTmToolTypeDO> pageResult = toolTypeService.getToolTypePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, MesTmToolTypeRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得工具类型精简列表", description = "主要用于前端的下拉选项")
    public CommonResult<List<MesTmToolTypeRespVO>> getToolTypeSimpleList() {
        List<MesTmToolTypeDO> list = toolTypeService.getToolTypeList();
        return success(BeanUtils.toBean(list, MesTmToolTypeRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出工具类型 Excel")
    @PreAuthorize("@ss.hasPermission('mes:tm-tool-type:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportToolTypeExcel(@Valid MesTmToolTypePageReqVO pageReqVO,
                                    HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MesTmToolTypeDO> list = toolTypeService.getToolTypePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "工具类型.xls", "数据", MesTmToolTypeRespVO.class,
                BeanUtils.toBean(list, MesTmToolTypeRespVO.class));
    }

}
