package cn.iocoder.yudao.module.crm.controller.admin.business;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.CrmBusinessPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.CrmBusinessRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.CrmBusinessSaveReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.CrmBusinessTransferReqVO;
import cn.iocoder.yudao.module.crm.convert.business.CrmBusinessConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessStatusDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessStatusTypeDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
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

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.CUSTOMER_NOT_EXISTS;

@Tag(name = "管理后台 - CRM 商机")
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
    public CommonResult<Long> createBusiness(@Valid @RequestBody CrmBusinessSaveReqVO createReqVO) {
        return success(businessService.createBusiness(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新商机")
    @PreAuthorize("@ss.hasPermission('crm:business:update')")
    public CommonResult<Boolean> updateBusiness(@Valid @RequestBody CrmBusinessSaveReqVO updateReqVO) {
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
        return success(BeanUtils.toBean(business, CrmBusinessRespVO.class));
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "获得商机列表")
    @Parameter(name = "ids", description = "编号", required = true, example = "[1024]")
    @PreAuthorize("@ss.hasPermission('crm:business:query')")
    public CommonResult<List<CrmBusinessRespVO>> getContactListByIds(@RequestParam("ids") List<Long> ids) {
        return success(BeanUtils.toBean(businessService.getBusinessList(ids, getLoginUserId()), CrmBusinessRespVO.class));
    }

    @GetMapping("/simple-all-list")
    @Operation(summary = "获得联系人的精简列表")
    @PreAuthorize("@ss.hasPermission('crm:contact:query')")
    public CommonResult<List<CrmBusinessRespVO>> getSimpleContactList() {
        CrmBusinessPageReqVO reqVO = new CrmBusinessPageReqVO();
        reqVO.setPageSize(PAGE_SIZE_NONE); // 不分页
        PageResult<CrmBusinessDO> pageResult = businessService.getBusinessPage(reqVO, getLoginUserId());
        return success(convertList(pageResult.getList(), business -> // 只返回 id、name 字段
                new CrmBusinessRespVO().setId(business.getId()).setName(business.getName())));
    }

    @GetMapping("/page")
    @Operation(summary = "获得商机分页")
    @PreAuthorize("@ss.hasPermission('crm:business:query')")
    public CommonResult<PageResult<CrmBusinessRespVO>> getBusinessPage(@Valid CrmBusinessPageReqVO pageVO) {
        PageResult<CrmBusinessDO> pageResult = businessService.getBusinessPage(pageVO, getLoginUserId());
        return success(buildBusinessDetailPageResult(pageResult));
    }

    @GetMapping("/page-by-customer")
    @Operation(summary = "获得商机分页，基于指定客户")
    public CommonResult<PageResult<CrmBusinessRespVO>> getBusinessPageByCustomer(@Valid CrmBusinessPageReqVO pageReqVO) {
        if (pageReqVO.getCustomerId() == null) {
            throw exception(CUSTOMER_NOT_EXISTS);
        }
        PageResult<CrmBusinessDO> pageResult = businessService.getBusinessPageByCustomerId(pageReqVO);
        return success(buildBusinessDetailPageResult(pageResult));
    }

    @GetMapping("/page-by-contact")
    @Operation(summary = "获得联系人的商机分页")
    @PreAuthorize("@ss.hasPermission('crm:business:query')")
    public CommonResult<PageResult<CrmBusinessRespVO>> getBusinessContactPage(@Valid CrmBusinessPageReqVO pageReqVO) {
        PageResult<CrmBusinessDO> pageResult = businessService.getBusinessPageByContact(pageReqVO);
        return success(buildBusinessDetailPageResult(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出商机 Excel")
    @PreAuthorize("@ss.hasPermission('crm:business:export')")
    @OperateLog(type = EXPORT)
    public void exportBusinessExcel(@Valid CrmBusinessPageReqVO exportReqVO,
                                    HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PAGE_SIZE_NONE);
        PageResult<CrmBusinessDO> pageResult = businessService.getBusinessPage(exportReqVO, getLoginUserId());
        // 导出 Excel
        ExcelUtils.write(response, "商机.xls", "数据", CrmBusinessRespVO.class,
                buildBusinessDetailPageResult(pageResult).getList());
    }

    /**
     * 构建详细的商机分页结果
     *
     * @param pageResult 简单的商机分页结果
     * @return 详细的商机分页结果
     */
    private PageResult<CrmBusinessRespVO> buildBusinessDetailPageResult(PageResult<CrmBusinessDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        List<CrmBusinessStatusTypeDO> statusTypeList = businessStatusTypeService.getBusinessStatusTypeList(
                convertSet(pageResult.getList(), CrmBusinessDO::getStatusTypeId));
        List<CrmBusinessStatusDO> statusList = businessStatusService.getBusinessStatusList(
                convertSet(pageResult.getList(), CrmBusinessDO::getStatusId));
        List<CrmCustomerDO> customerList = customerService.getCustomerList(
                convertSet(pageResult.getList(), CrmBusinessDO::getCustomerId));
        return CrmBusinessConvert.INSTANCE.convertPage(pageResult, customerList, statusTypeList, statusList);
    }

    @PutMapping("/transfer")
    @Operation(summary = "商机转移")
    @PreAuthorize("@ss.hasPermission('crm:business:update')")
    public CommonResult<Boolean> transferBusiness(@Valid @RequestBody CrmBusinessTransferReqVO reqVO) {
        businessService.transferBusiness(reqVO, getLoginUserId());
        return success(true);
    }

}
