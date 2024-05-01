package cn.iocoder.yudao.module.crm.controller.admin.business;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessProductDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessStatusDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessStatusTypeDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.product.CrmProductDO;
import cn.iocoder.yudao.module.crm.service.business.CrmBusinessService;
import cn.iocoder.yudao.module.crm.service.business.CrmBusinessStatusService;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
import cn.iocoder.yudao.module.crm.service.product.CrmProductService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
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
    private CrmBusinessStatusService businessStatusTypeService;
    @Resource
    private CrmBusinessStatusService businessStatusService;
    @Resource
    private CrmProductService productService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

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

    @PutMapping("/update-status")
    @Operation(summary = "更新商机状态")
    @PreAuthorize("@ss.hasPermission('crm:business:update')")
    public CommonResult<Boolean> updateBusinessStatus(@Valid @RequestBody CrmBusinessUpdateStatusReqVO updateStatusReqVO) {
        businessService.updateBusinessStatus(updateStatusReqVO);
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
        return success(buildBusinessDetail(business));
    }

    private CrmBusinessRespVO buildBusinessDetail(CrmBusinessDO business) {
        if (business == null) {
            return null;
        }
        CrmBusinessRespVO businessVO = buildBusinessDetailList(Collections.singletonList(business)).get(0);
        // 拼接产品项
        List<CrmBusinessProductDO> businessProducts = businessService.getBusinessProductListByBusinessId(businessVO.getId());
        Map<Long, CrmProductDO> productMap = productService.getProductMap(
                convertSet(businessProducts, CrmBusinessProductDO::getProductId));
        businessVO.setProducts(BeanUtils.toBean(businessProducts, CrmBusinessRespVO.Product.class, businessProductVO ->
                MapUtils.findAndThen(productMap, businessProductVO.getProductId(),
                        product -> businessProductVO.setProductName(product.getName())
                                .setProductNo(product.getNo()).setProductUnit(product.getUnit()))));
        return businessVO;
    }

    @GetMapping("/simple-all-list")
    @Operation(summary = "获得联系人的精简列表")
    @PreAuthorize("@ss.hasPermission('crm:contact:query')")
    public CommonResult<List<CrmBusinessRespVO>> getSimpleContactList() {
        CrmBusinessPageReqVO reqVO = new CrmBusinessPageReqVO();
        reqVO.setPageSize(PAGE_SIZE_NONE); // 不分页
        PageResult<CrmBusinessDO> pageResult = businessService.getBusinessPage(reqVO, getLoginUserId());
        return success(convertList(pageResult.getList(), business -> // 只返回 id、name 字段
                new CrmBusinessRespVO().setId(business.getId()).setName(business.getName())
                        .setCustomerId(business.getCustomerId())));
    }

    @GetMapping("/page")
    @Operation(summary = "获得商机分页")
    @PreAuthorize("@ss.hasPermission('crm:business:query')")
    public CommonResult<PageResult<CrmBusinessRespVO>> getBusinessPage(@Valid CrmBusinessPageReqVO pageVO) {
        PageResult<CrmBusinessDO> pageResult = businessService.getBusinessPage(pageVO, getLoginUserId());
        return success(new PageResult<>(buildBusinessDetailList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/page-by-customer")
    @Operation(summary = "获得商机分页，基于指定客户")
    public CommonResult<PageResult<CrmBusinessRespVO>> getBusinessPageByCustomer(@Valid CrmBusinessPageReqVO pageReqVO) {
        if (pageReqVO.getCustomerId() == null) {
            throw exception(CUSTOMER_NOT_EXISTS);
        }
        PageResult<CrmBusinessDO> pageResult = businessService.getBusinessPageByCustomerId(pageReqVO);
        return success(new PageResult<>(buildBusinessDetailList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/page-by-contact")
    @Operation(summary = "获得联系人的商机分页")
    @PreAuthorize("@ss.hasPermission('crm:business:query')")
    public CommonResult<PageResult<CrmBusinessRespVO>> getBusinessContactPage(@Valid CrmBusinessPageReqVO pageReqVO) {
        PageResult<CrmBusinessDO> pageResult = businessService.getBusinessPageByContact(pageReqVO);
        return success(new PageResult<>(buildBusinessDetailList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出商机 Excel")
    @PreAuthorize("@ss.hasPermission('crm:business:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportBusinessExcel(@Valid CrmBusinessPageReqVO exportReqVO,
                                    HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PAGE_SIZE_NONE);
        List<CrmBusinessDO> list = businessService.getBusinessPage(exportReqVO, getLoginUserId()).getList();
        // 导出 Excel
        ExcelUtils.write(response, "商机.xls", "数据", CrmBusinessRespVO.class,
                buildBusinessDetailList(list));
    }

    public List<CrmBusinessRespVO> buildBusinessDetailList(List<CrmBusinessDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1.1 获取客户列表
        Map<Long, CrmCustomerDO> customerMap = customerService.getCustomerMap(
                convertSet(list, CrmBusinessDO::getCustomerId));
        // 1.2 获取创建人、负责人列表
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertListByFlatMap(list,
                contact -> Stream.of(NumberUtils.parseLong(contact.getCreator()), contact.getOwnerUserId())));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(userMap.values(), AdminUserRespDTO::getDeptId));
        // 1.3 获得商机状态组
        Map<Long, CrmBusinessStatusTypeDO> statusTypeMap = businessStatusTypeService.getBusinessStatusTypeMap(
                convertSet(list, CrmBusinessDO::getStatusTypeId));
        Map<Long, CrmBusinessStatusDO> statusMap = businessStatusService.getBusinessStatusMap(
                convertSet(list, CrmBusinessDO::getStatusId));
        // 2. 拼接数据
        return BeanUtils.toBean(list, CrmBusinessRespVO.class, businessVO -> {
            // 2.1 设置客户名称
            MapUtils.findAndThen(customerMap, businessVO.getCustomerId(), customer -> businessVO.setCustomerName(customer.getName()));
            // 2.2 设置创建人、负责人名称
            MapUtils.findAndThen(userMap, NumberUtils.parseLong(businessVO.getCreator()),
                    user -> businessVO.setCreatorName(user.getNickname()));
            MapUtils.findAndThen(userMap, businessVO.getOwnerUserId(), user -> {
                businessVO.setOwnerUserName(user.getNickname());
                MapUtils.findAndThen(deptMap, user.getDeptId(), dept -> businessVO.setOwnerUserDeptName(dept.getName()));
            });
            // 2.3 设置商机状态
            MapUtils.findAndThen(statusTypeMap, businessVO.getStatusTypeId(), statusType -> businessVO.setStatusTypeName(statusType.getName()));
            MapUtils.findAndThen(statusMap, businessVO.getStatusId(), status -> businessVO.setStatusName(
                    businessService.getBusinessStatusName(businessVO.getEndStatus(), status)));
        });
    }

    @PutMapping("/transfer")
    @Operation(summary = "商机转移")
    @PreAuthorize("@ss.hasPermission('crm:business:update')")
    public CommonResult<Boolean> transferBusiness(@Valid @RequestBody CrmBusinessTransferReqVO reqVO) {
        businessService.transferBusiness(reqVO, getLoginUserId());
        return success(true);
    }

}
