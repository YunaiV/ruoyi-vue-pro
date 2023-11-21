package cn.iocoder.yudao.module.crm.controller.admin.receivable;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.*;
import cn.iocoder.yudao.module.crm.convert.receivable.CrmReceivablePlanConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.CrmReceivablePlanDO;
import cn.iocoder.yudao.module.crm.service.receivable.CrmReceivablePlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "管理后台 - CRM 回款计划")
@RestController
@RequestMapping("/crm/receivable-plan")
@Validated
public class CrmReceivablePlanController {

    @Resource
    private CrmReceivablePlanService crmReceivablePlanService;

    @PostMapping("/create")
    @Operation(summary = "创建回款计划")
    @PreAuthorize("@ss.hasPermission('crm:receivable-plan:create')")
    public CommonResult<Long> createReceivablePlan(@Valid @RequestBody CrmReceivablePlanCreateReqVO createReqVO) {
        return success(crmReceivablePlanService.createReceivablePlan(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新回款计划")
    @PreAuthorize("@ss.hasPermission('crm:receivable-plan:update')")
    public CommonResult<Boolean> updateReceivablePlan(@Valid @RequestBody CrmReceivablePlanUpdateReqVO updateReqVO) {
        crmReceivablePlanService.updateReceivablePlan(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除回款计划")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('crm:receivable-plan:delete')")
    public CommonResult<Boolean> deleteReceivablePlan(@RequestParam("id") Long id) {
        crmReceivablePlanService.deleteReceivablePlan(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得回款计划")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:receivable-plan:query')")
    public CommonResult<CrmReceivablePlanRespVO> getReceivablePlan(@RequestParam("id") Long id) {
        CrmReceivablePlanDO receivablePlan = crmReceivablePlanService.getReceivablePlan(id);
        return success(CrmReceivablePlanConvert.INSTANCE.convert(receivablePlan));
    }

    @GetMapping("/page")
    @Operation(summary = "获得回款计划分页")
    @PreAuthorize("@ss.hasPermission('crm:receivable-plan:query')")
    public CommonResult<PageResult<CrmReceivablePlanRespVO>> getReceivablePlanPage(@Valid CrmReceivablePlanPageReqVO pageVO) {
        PageResult<CrmReceivablePlanDO> pageResult = crmReceivablePlanService.getReceivablePlanPage(pageVO);
        return success(CrmReceivablePlanConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出回款计划 Excel")
    @PreAuthorize("@ss.hasPermission('crm:receivable-plan:export')")
    @OperateLog(type = EXPORT)
    public void exportReceivablePlanExcel(@Valid CrmReceivablePlanExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<CrmReceivablePlanDO> list = crmReceivablePlanService.getReceivablePlanList(exportReqVO);
        // 导出 Excel
        List<CrmReceivablePlanExcelVO> datas = CrmReceivablePlanConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "回款计划.xls", "数据", CrmReceivablePlanExcelVO.class, datas);
    }

}
