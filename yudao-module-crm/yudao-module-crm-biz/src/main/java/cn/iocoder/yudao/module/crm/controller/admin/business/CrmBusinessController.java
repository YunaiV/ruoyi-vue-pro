package cn.iocoder.yudao.module.crm.controller.admin.business;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.*;
import cn.iocoder.yudao.module.crm.convert.business.CrmBusinessConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessStatusDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessStatusTypeDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.crm.service.business.CrmBusinessService;
import cn.iocoder.yudao.module.crm.service.business.CrmBusinessStatusService;
import cn.iocoder.yudao.module.crm.service.business.CrmBusinessStatusTypeService;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 商机")
@RestController
@RequestMapping("/crm/business")
@Validated
public class CrmBusinessController {

    @Resource
    private CrmBusinessService businessService;

    @Resource
    private CrmCustomerService customerService;

    @Resource
    private CrmBusinessStatusTypeService businessStatusTypeService;

    @Resource
    private CrmBusinessStatusService businessStatusService;

    @PostMapping("/create")
    @Operation(summary = "创建商机")
    @PreAuthorize("@ss.hasPermission('crm:business:create')")
    public CommonResult<Long> createBusiness(@Valid @RequestBody CrmBusinessCreateReqVO createReqVO) {
        return success(businessService.createBusiness(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新商机")
    @PreAuthorize("@ss.hasPermission('crm:business:update')")
    public CommonResult<Boolean> updateBusiness(@Valid @RequestBody CrmBusinessUpdateReqVO updateReqVO) {
        businessService.updateBusiness(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除商机")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('crm:business:delete')")
    public CommonResult<Boolean> deleteBusiness(@RequestParam("id") Long id) {
        businessService.deleteBusiness(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得商机")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:business:query')")
    public CommonResult<CrmBusinessRespVO> getBusiness(@RequestParam("id") Long id) {
        CrmBusinessDO business = businessService.getBusiness(id);
        return success(CrmBusinessConvert.INSTANCE.convert(business));
    }

    @GetMapping("/page")
    @Operation(summary = "获得商机分页")
    @PreAuthorize("@ss.hasPermission('crm:business:query')")
    public CommonResult<PageResult<CrmBusinessRespVO>> getBusinessPage(@Valid CrmBusinessPageReqVO pageVO) {
        PageResult<CrmBusinessDO> pageResult = businessService.getBusinessPage(pageVO, getLoginUserId());
        //处理客户名称回显
        Set<Long> customerIds = pageResult.getList().stream()
                .map(CrmBusinessDO::getCustomerId).filter(Objects::nonNull).collect(Collectors.toSet());
        List<CrmCustomerDO> customerList = customerService.getCustomerList(customerIds);
        //处理商机状态类型名称回显
        Set<Long> statusTypeIds = pageResult.getList().stream()
                .map(CrmBusinessDO::getStatusTypeId).filter(Objects::nonNull).collect(Collectors.toSet());
        CrmBusinessStatusTypeQueryVO queryStatusTypeVO = new CrmBusinessStatusTypeQueryVO();
        queryStatusTypeVO.setIdList(statusTypeIds);
        List<CrmBusinessStatusTypeDO> statusTypeList = businessStatusTypeService.selectList(queryStatusTypeVO);
        //处理商机状态名称回显
        Set<Long> statusIds = pageResult.getList().stream()
                .map(CrmBusinessDO::getCustomerId).filter(Objects::nonNull).collect(Collectors.toSet());
        CrmBusinessStatusQueryVO queryVO = new CrmBusinessStatusQueryVO();
        queryVO.setIdList(statusIds);
        List<CrmBusinessStatusDO> statusList = businessStatusService.selectList(queryVO);
        return success(CrmBusinessConvert.INSTANCE.convertPage(pageResult, customerList, statusTypeList, statusList));
    }

    @GetMapping("/pool-page")
    @Operation(summary = "获得商机公海分页")
    @PreAuthorize("@ss.hasPermission('crm:business:query')")
    public CommonResult<PageResult<CrmBusinessRespVO>> getBusinessPoolPage(@Valid CrmBusinessPageReqVO pageVO) {
        // TODO puhui999: 等数据权限完善后再实现
        return null;
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出商机 Excel")
    @PreAuthorize("@ss.hasPermission('crm:business:export')")
    @OperateLog(type = EXPORT)
    public void exportBusinessExcel(@Valid CrmBusinessExportReqVO exportReqVO,
                                    HttpServletResponse response) throws IOException {
        List<CrmBusinessDO> list = businessService.getBusinessList(exportReqVO);
        // 导出 Excel
        List<CrmBusinessExcelVO> datas = CrmBusinessConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "商机.xls", "数据", CrmBusinessExcelVO.class, datas);
    }

    @PutMapping("/transfer")
    @Operation(summary = "商机转移")
    @PreAuthorize("@ss.hasPermission('crm:business:update')")
    public CommonResult<Boolean> transfer(@Valid @RequestBody CrmBusinessTransferReqVO reqVO) {
        businessService.transferBusiness(reqVO, getLoginUserId());
        return success(true);
    }

}
