package cn.iocoder.yudao.module.mes.controller.admin.qc.indicator;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.qc.indicator.vo.MesQcIndicatorPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.qc.indicator.vo.MesQcIndicatorRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.qc.indicator.vo.MesQcIndicatorSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.indicator.MesQcIndicatorDO;
import cn.iocoder.yudao.module.mes.service.qc.indicator.MesQcIndicatorService;
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

@Tag(name = "管理后台 - MES 质检指标")
@RestController
@RequestMapping("/mes/qc/indicator")
@Validated
public class MesQcIndicatorController {

    @Resource
    private MesQcIndicatorService indicatorService;

    @PostMapping("/create")
    @Operation(summary = "创建质检指标")
    @PreAuthorize("@ss.hasPermission('mes:qc-indicator:create')")
    public CommonResult<Long> createIndicator(@Valid @RequestBody MesQcIndicatorSaveReqVO createReqVO) {
        return success(indicatorService.createIndicator(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新质检指标")
    @PreAuthorize("@ss.hasPermission('mes:qc-indicator:update')")
    public CommonResult<Boolean> updateIndicator(@Valid @RequestBody MesQcIndicatorSaveReqVO updateReqVO) {
        indicatorService.updateIndicator(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除质检指标")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:qc-indicator:delete')")
    public CommonResult<Boolean> deleteIndicator(@RequestParam("id") Long id) {
        indicatorService.deleteIndicator(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得质检指标")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:qc-indicator:query')")
    public CommonResult<MesQcIndicatorRespVO> getIndicator(@RequestParam("id") Long id) {
        MesQcIndicatorDO indicator = indicatorService.getIndicator(id);
        return success(BeanUtils.toBean(indicator, MesQcIndicatorRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得质检指标分页")
    @PreAuthorize("@ss.hasPermission('mes:qc-indicator:query')")
    public CommonResult<PageResult<MesQcIndicatorRespVO>> getIndicatorPage(@Valid MesQcIndicatorPageReqVO pageReqVO) {
        PageResult<MesQcIndicatorDO> pageResult = indicatorService.getIndicatorPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, MesQcIndicatorRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出质检指标 Excel")
    @PreAuthorize("@ss.hasPermission('mes:qc-indicator:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportIndicatorExcel(@Valid MesQcIndicatorPageReqVO pageReqVO,
                                     HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MesQcIndicatorDO> list = indicatorService.getIndicatorPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "质检指标.xls", "数据", MesQcIndicatorRespVO.class,
                BeanUtils.toBean(list, MesQcIndicatorRespVO.class));
    }

}
