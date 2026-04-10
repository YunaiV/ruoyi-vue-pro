package cn.iocoder.yudao.module.mes.controller.admin.dv.checkplan;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.dv.checkplan.vo.MesDvCheckPlanPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.checkplan.vo.MesDvCheckPlanRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.checkplan.vo.MesDvCheckPlanSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.checkplan.MesDvCheckPlanDO;
import cn.iocoder.yudao.module.mes.service.dv.checkplan.MesDvCheckPlanService;
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

@Tag(name = "管理后台 - MES 点检保养方案")
@RestController
@RequestMapping("/mes/dv/check-plan")
@Validated
public class MesDvCheckPlanController {

    @Resource
    private MesDvCheckPlanService checkPlanService;

    @PostMapping("/create")
    @Operation(summary = "创建点检保养方案")
    @PreAuthorize("@ss.hasPermission('mes:dv-check-plan:create')")
    public CommonResult<Long> createCheckPlan(@Valid @RequestBody MesDvCheckPlanSaveReqVO createReqVO) {
        return success(checkPlanService.createCheckPlan(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新点检保养方案")
    @PreAuthorize("@ss.hasPermission('mes:dv-check-plan:update')")
    public CommonResult<Boolean> updateCheckPlan(@Valid @RequestBody MesDvCheckPlanSaveReqVO updateReqVO) {
        checkPlanService.updateCheckPlan(updateReqVO);
        return success(true);
    }

    @PutMapping("/enable")
    @Operation(summary = "启用点检保养方案")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:dv-check-plan:update')")
    public CommonResult<Boolean> enableCheckPlan(@RequestParam("id") Long id) {
        checkPlanService.enableCheckPlan(id);
        return success(true);
    }

    @PutMapping("/disable")
    @Operation(summary = "停用点检保养方案")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:dv-check-plan:update')")
    public CommonResult<Boolean> disableCheckPlan(@RequestParam("id") Long id) {
        checkPlanService.disableCheckPlan(id);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除点检保养方案")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:dv-check-plan:delete')")
    public CommonResult<Boolean> deleteCheckPlan(@RequestParam("id") Long id) {
        checkPlanService.deleteCheckPlan(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得点检保养方案")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:dv-check-plan:query')")
    public CommonResult<MesDvCheckPlanRespVO> getCheckPlan(@RequestParam("id") Long id) {
        MesDvCheckPlanDO checkPlan = checkPlanService.getCheckPlan(id);
        return success(BeanUtils.toBean(checkPlan, MesDvCheckPlanRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得点检保养方案分页")
    @PreAuthorize("@ss.hasPermission('mes:dv-check-plan:query')")
    public CommonResult<PageResult<MesDvCheckPlanRespVO>> getCheckPlanPage(@Valid MesDvCheckPlanPageReqVO pageReqVO) {
        PageResult<MesDvCheckPlanDO> pageResult = checkPlanService.getCheckPlanPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, MesDvCheckPlanRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出点检保养方案 Excel")
    @PreAuthorize("@ss.hasPermission('mes:dv-check-plan:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportCheckPlanExcel(@Valid MesDvCheckPlanPageReqVO pageReqVO,
                                      HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MesDvCheckPlanDO> list = checkPlanService.getCheckPlanPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "点检保养方案.xls", "数据", MesDvCheckPlanRespVO.class,
                BeanUtils.toBean(list, MesDvCheckPlanRespVO.class));
    }

}
