package cn.iocoder.yudao.module.crm.controller.admin.businessstatus;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatus.vo.*;
import cn.iocoder.yudao.module.crm.convert.businessstatus.CrmBusinessStatusConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.businessstatus.CrmBusinessStatusDO;
import cn.iocoder.yudao.module.crm.service.businessstatus.CrmBusinessStatusService;
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
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

// TODO @lilleo：这个模块，可以挪到 business 下；这样我打开 business 包下，就知道，噢~原来里面有 business 商机、有 type 状态类型、status 具体状态；
@Tag(name = "管理后台 - 商机状态")
@RestController
@RequestMapping("/crm/business-status")
@Validated
public class CrmBusinessStatusController {

    @Resource
    private CrmBusinessStatusService businessStatusService;

    @PostMapping("/create")
    @Operation(summary = "创建商机状态")
    @PreAuthorize("@ss.hasPermission('crm:business-status:create')")
    public CommonResult<Long> createBusinessStatus(@Valid @RequestBody CrmBusinessStatusCreateReqVO createReqVO) {
        return success(businessStatusService.createBusinessStatus(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新商机状态")
    @PreAuthorize("@ss.hasPermission('crm:business-status:update')")
    public CommonResult<Boolean> updateBusinessStatus(@Valid @RequestBody CrmBusinessStatusUpdateReqVO updateReqVO) {
        businessStatusService.updateBusinessStatus(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除商机状态")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('crm:business-status:delete')")
    public CommonResult<Boolean> deleteBusinessStatus(@RequestParam("id") Long id) {
        businessStatusService.deleteBusinessStatus(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得商机状态")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:business-status:query')")
    public CommonResult<CrmBusinessStatusRespVO> getBusinessStatus(@RequestParam("id") Long id) {
        CrmBusinessStatusDO businessStatus = businessStatusService.getBusinessStatus(id);
        return success(CrmBusinessStatusConvert.INSTANCE.convert(businessStatus));
    }

    // TODO @lilleo：这个接口，暂时用不到，可以考虑先删除掉
    @GetMapping("/list")
    @Operation(summary = "获得商机状态列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('crm:business-status:query')")
    public CommonResult<List<CrmBusinessStatusRespVO>> getBusinessStatusList(@RequestParam("ids") Collection<Long> ids) {
        List<CrmBusinessStatusDO> list = businessStatusService.getBusinessStatusList(ids);
        return success(CrmBusinessStatusConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得商机状态分页")
    @PreAuthorize("@ss.hasPermission('crm:business-status:query')")
    public CommonResult<PageResult<CrmBusinessStatusRespVO>> getBusinessStatusPage(@Valid CrmBusinessStatusPageReqVO pageVO) {
        PageResult<CrmBusinessStatusDO> pageResult = businessStatusService.getBusinessStatusPage(pageVO);
        return success(CrmBusinessStatusConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出商机状态 Excel")
    @PreAuthorize("@ss.hasPermission('crm:business-status:export')")
    @OperateLog(type = EXPORT)
    public void exportBusinessStatusExcel(@Valid CrmBusinessStatusExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<CrmBusinessStatusDO> list = businessStatusService.getBusinessStatusList(exportReqVO);
        // 导出 Excel
        List<CrmBusinessStatusExcelVO> datas = CrmBusinessStatusConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "商机状态.xls", "数据", CrmBusinessStatusExcelVO.class, datas);
    }

    // TODO 芋艿：后续再看看
    @GetMapping("/get-simple-list")
    @Operation(summary = "获得商机状态列表")
    @PreAuthorize("@ss.hasPermission('crm:business-status:query')")
    public CommonResult<List<CrmBusinessStatusRespVO>> getBusinessStatusListByTypeId(@RequestParam("typeId") Integer typeId) {
        List<CrmBusinessStatusDO> list = businessStatusService.getBusinessStatusListByTypeId(typeId);
        return success(CrmBusinessStatusConvert.INSTANCE.convertList(list));
    }

    // TODO 芋艿：后续再看看
    @GetMapping("/get-all-list")
    @Operation(summary = "获得商机状态列表")
    @PreAuthorize("@ss.hasPermission('crm:business-status:query')")
    public CommonResult<List<CrmBusinessStatusRespVO>> getBusinessStatusList() {
        List<CrmBusinessStatusDO> list = businessStatusService.getBusinessStatusList();
        return success(CrmBusinessStatusConvert.INSTANCE.convertList(list));
    }

}
