package cn.iocoder.yudao.module.crm.controller.admin.businessstatustype;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatustype.vo.*;
import cn.iocoder.yudao.module.crm.convert.businessstatustype.CrmBusinessStatusTypeConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.businessstatustype.CrmBusinessStatusTypeDO;
import cn.iocoder.yudao.module.crm.service.businessstatustype.CrmBusinessStatusTypeService;
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
@Tag(name = "管理后台 - 商机状态类型")
@RestController
@RequestMapping("/crm/business-status-type")
@Validated
public class CrmBusinessStatusTypeController {

    @Resource
    private CrmBusinessStatusTypeService businessStatusTypeService;

    @PostMapping("/create")
    @Operation(summary = "创建商机状态类型")
    @PreAuthorize("@ss.hasPermission('crm:business-status-type:create')")
    public CommonResult<Long> createBusinessStatusType(@Valid @RequestBody CrmBusinessStatusTypeCreateReqVO createReqVO) {
        return success(businessStatusTypeService.createBusinessStatusType(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新商机状态类型")
    @PreAuthorize("@ss.hasPermission('crm:business-status-type:update')")
    public CommonResult<Boolean> updateBusinessStatusType(@Valid @RequestBody CrmBusinessStatusTypeUpdateReqVO updateReqVO) {
        businessStatusTypeService.updateBusinessStatusType(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除商机状态类型")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('crm:business-status-type:delete')")
    public CommonResult<Boolean> deleteBusinessStatusType(@RequestParam("id") Long id) {
        businessStatusTypeService.deleteBusinessStatusType(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得商机状态类型")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:business-status-type:query')")
    public CommonResult<CrmBusinessStatusTypeRespVO> getBusinessStatusType(@RequestParam("id") Long id) {
        CrmBusinessStatusTypeDO businessStatusType = businessStatusTypeService.getBusinessStatusType(id);
        return success(CrmBusinessStatusTypeConvert.INSTANCE.convert(businessStatusType));
    }

    // TODO @lilleo：这个接口，暂时用不到，可以考虑先删除掉
    @GetMapping("/list")
    @Operation(summary = "获得商机状态类型列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('crm:business-status-type:query')")
    public CommonResult<List<CrmBusinessStatusTypeRespVO>> getBusinessStatusTypeList(@RequestParam("ids") Collection<Long> ids) {
        List<CrmBusinessStatusTypeDO> list = businessStatusTypeService.getBusinessStatusTypeList(ids);
        return success(CrmBusinessStatusTypeConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得商机状态类型分页")
    @PreAuthorize("@ss.hasPermission('crm:business-status-type:query')")
    public CommonResult<PageResult<CrmBusinessStatusTypeRespVO>> getBusinessStatusTypePage(@Valid CrmBusinessStatusTypePageReqVO pageVO) {
        PageResult<CrmBusinessStatusTypeDO> pageResult = businessStatusTypeService.getBusinessStatusTypePage(pageVO);
        return success(CrmBusinessStatusTypeConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出商机状态类型 Excel")
    @PreAuthorize("@ss.hasPermission('crm:business-status-type:export')")
    @OperateLog(type = EXPORT)
    public void exportBusinessStatusTypeExcel(@Valid CrmBusinessStatusTypeExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<CrmBusinessStatusTypeDO> list = businessStatusTypeService.getBusinessStatusTypeList(exportReqVO);
        // 导出 Excel
        List<CrmBusinessStatusTypeExcelVO> datas = CrmBusinessStatusTypeConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "商机状态类型.xls", "数据", CrmBusinessStatusTypeExcelVO.class, datas);
    }

    @GetMapping("/get-simple-list")
    @Operation(summary = "获得商机状态类型列表")
    @PreAuthorize("@ss.hasPermission('crm:business-status-type:query')")
    public CommonResult<List<CrmBusinessStatusTypeRespVO>> getBusinessStatusTypeList() {
        List<CrmBusinessStatusTypeDO> list = businessStatusTypeService.getBusinessStatusTypeListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(CrmBusinessStatusTypeConvert.INSTANCE.convertList(list));
    }

}
