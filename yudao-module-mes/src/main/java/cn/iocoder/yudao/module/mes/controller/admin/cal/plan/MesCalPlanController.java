package cn.iocoder.yudao.module.mes.controller.admin.cal.plan;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.cal.plan.vo.MesCalPlanPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.cal.plan.vo.MesCalPlanRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.cal.plan.vo.MesCalPlanSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanDO;
import cn.iocoder.yudao.module.mes.service.cal.plan.MesCalPlanService;
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

@Tag(name = "管理后台 - MES 排班计划")
@RestController
@RequestMapping("/mes/cal/plan")
@Validated
public class MesCalPlanController {

    @Resource
    private MesCalPlanService planService;

    @PostMapping("/create")
    @Operation(summary = "创建排班计划")
    @PreAuthorize("@ss.hasPermission('mes:cal-plan:create')")
    public CommonResult<Long> createPlan(@Valid @RequestBody MesCalPlanSaveReqVO createReqVO) {
        return success(planService.createPlan(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新排班计划")
    @PreAuthorize("@ss.hasPermission('mes:cal-plan:update')")
    public CommonResult<Boolean> updatePlan(@Valid @RequestBody MesCalPlanSaveReqVO updateReqVO) {
        planService.updatePlan(updateReqVO);
        return success(true);
    }

    @PutMapping("/confirm")
    @Operation(summary = "确认排班计划")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:cal-plan:update')")
    public CommonResult<Boolean> confirmPlan(@RequestParam("id") Long id) {
        planService.confirmPlan(id);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除排班计划")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:cal-plan:delete')")
    public CommonResult<Boolean> deletePlan(@RequestParam("id") Long id) {
        planService.deletePlan(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得排班计划")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:cal-plan:query')")
    public CommonResult<MesCalPlanRespVO> getPlan(@RequestParam("id") Long id) {
        MesCalPlanDO plan = planService.getPlan(id);
        return success(BeanUtils.toBean(plan, MesCalPlanRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得排班计划分页")
    @PreAuthorize("@ss.hasPermission('mes:cal-plan:query')")
    public CommonResult<PageResult<MesCalPlanRespVO>> getPlanPage(@Valid MesCalPlanPageReqVO pageReqVO) {
        PageResult<MesCalPlanDO> pageResult = planService.getPlanPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, MesCalPlanRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出排班计划 Excel")
    @PreAuthorize("@ss.hasPermission('mes:cal-plan:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPlanExcel(@Valid MesCalPlanPageReqVO pageReqVO,
                                HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MesCalPlanDO> list = planService.getPlanPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "排班计划.xls", "数据", MesCalPlanRespVO.class,
                BeanUtils.toBean(list, MesCalPlanRespVO.class));
    }

}
