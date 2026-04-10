package cn.iocoder.yudao.module.mes.controller.admin.qc.defect;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.qc.defect.vo.MesQcDefectPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.qc.defect.vo.MesQcDefectRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.qc.defect.vo.MesQcDefectSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.defect.MesQcDefectDO;
import cn.iocoder.yudao.module.mes.service.qc.defect.MesQcDefectService;
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

@Tag(name = "管理后台 - MES 缺陷类型")
@RestController
@RequestMapping("/mes/qc/defect")
@Validated
public class MesQcDefectController {

    @Resource
    private MesQcDefectService defectService;

    @PostMapping("/create")
    @Operation(summary = "创建缺陷类型")
    @PreAuthorize("@ss.hasPermission('mes:qc-defect:create')")
    public CommonResult<Long> createDefect(@Valid @RequestBody MesQcDefectSaveReqVO createReqVO) {
        return success(defectService.createDefect(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新缺陷类型")
    @PreAuthorize("@ss.hasPermission('mes:qc-defect:update')")
    public CommonResult<Boolean> updateDefect(@Valid @RequestBody MesQcDefectSaveReqVO updateReqVO) {
        defectService.updateDefect(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除缺陷类型")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:qc-defect:delete')")
    public CommonResult<Boolean> deleteDefect(@RequestParam("id") Long id) {
        defectService.deleteDefect(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得缺陷类型")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:qc-defect:query')")
    public CommonResult<MesQcDefectRespVO> getDefect(@RequestParam("id") Long id) {
        MesQcDefectDO defect = defectService.getDefect(id);
        return success(BeanUtils.toBean(defect, MesQcDefectRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得缺陷类型分页")
    @PreAuthorize("@ss.hasPermission('mes:qc-defect:query')")
    public CommonResult<PageResult<MesQcDefectRespVO>> getDefectPage(@Valid MesQcDefectPageReqVO pageReqVO) {
        PageResult<MesQcDefectDO> pageResult = defectService.getDefectPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, MesQcDefectRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得缺陷类型精简列表", description = "主要用于前端的下拉选项")
    public CommonResult<List<MesQcDefectRespVO>> getDefectSimpleList() {
        List<MesQcDefectDO> list = defectService.getDefectList();
        return success(BeanUtils.toBean(list, MesQcDefectRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出缺陷类型 Excel")
    @PreAuthorize("@ss.hasPermission('mes:qc-defect:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportDefectExcel(@Valid MesQcDefectPageReqVO pageReqVO,
                                  HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MesQcDefectDO> list = defectService.getDefectPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "缺陷类型.xls", "数据", MesQcDefectRespVO.class,
                BeanUtils.toBean(list, MesQcDefectRespVO.class));
    }

}
