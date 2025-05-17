package cn.iocoder.yudao.module.crm.controller.admin.contract;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.contract.CrmContractPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.contract.CrmContractRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.contract.CrmContractSaveReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.contract.CrmContractTransferReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.CrmContactDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.CrmContractDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.CrmContractProductDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.product.CrmProductDO;
import cn.iocoder.yudao.module.crm.service.business.CrmBusinessService;
import cn.iocoder.yudao.module.crm.service.contact.CrmContactService;
import cn.iocoder.yudao.module.crm.service.contract.CrmContractService;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
import cn.iocoder.yudao.module.crm.service.product.CrmProductService;
import cn.iocoder.yudao.module.crm.service.receivable.CrmReceivableService;
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
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static java.util.Collections.singletonList;

@Tag(name = "管理后台 - CRM 合同")
@RestController
@RequestMapping("/crm/contract")
@Validated
public class CrmContractController {

    @Resource
    private CrmContractService contractService;
    @Resource
    private CrmCustomerService customerService;
    @Resource
    private CrmContactService contactService;
    @Resource
    private CrmBusinessService businessService;
    @Resource
    private CrmProductService productService;
    @Resource
    private CrmReceivableService receivableService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @PostMapping("/create")
    @Operation(summary = "创建合同")
    @PreAuthorize("@ss.hasPermission('crm:contract:create')")
    public CommonResult<Long> createContract(@Valid @RequestBody CrmContractSaveReqVO createReqVO) {
        return success(contractService.createContract(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新合同")
    @PreAuthorize("@ss.hasPermission('crm:contract:update')")
    public CommonResult<Boolean> updateContract(@Valid @RequestBody CrmContractSaveReqVO updateReqVO) {
        contractService.updateContract(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除合同")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('crm:contract:delete')")
    public CommonResult<Boolean> deleteContract(@RequestParam("id") Long id) {
        contractService.deleteContract(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得合同")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:contract:query')")
    public CommonResult<CrmContractRespVO> getContract(@RequestParam("id") Long id) {
        CrmContractDO contract = contractService.getContract(id);
        return success(buildContractDetail(contract));
    }

    private CrmContractRespVO buildContractDetail(CrmContractDO contract) {
        if (contract == null) {
            return null;
        }
        CrmContractRespVO contractVO = buildContractDetailList(singletonList(contract)).get(0);
        // 拼接产品项
        List<CrmContractProductDO> businessProducts = contractService.getContractProductListByContractId(contractVO.getId());
        Map<Long, CrmProductDO> productMap = productService.getProductMap(
                convertSet(businessProducts, CrmContractProductDO::getProductId));
        contractVO.setProducts(BeanUtils.toBean(businessProducts, CrmContractRespVO.Product.class, businessProductVO ->
                MapUtils.findAndThen(productMap, businessProductVO.getProductId(),
                        product -> businessProductVO.setProductName(product.getName())
                                .setProductNo(product.getNo()).setProductUnit(product.getUnit()))));
        return contractVO;
    }

    @GetMapping("/page")
    @Operation(summary = "获得合同分页")
    @PreAuthorize("@ss.hasPermission('crm:contract:query')")
    public CommonResult<PageResult<CrmContractRespVO>> getContractPage(@Valid CrmContractPageReqVO pageVO) {
        PageResult<CrmContractDO> pageResult = contractService.getContractPage(pageVO, getLoginUserId());
        return success(BeanUtils.toBean(pageResult, CrmContractRespVO.class).setList(buildContractDetailList(pageResult.getList())));
    }

    @GetMapping("/page-by-customer")
    @Operation(summary = "获得合同分页，基于指定客户")
    public CommonResult<PageResult<CrmContractRespVO>> getContractPageByCustomer(@Valid CrmContractPageReqVO pageVO) {
        Assert.notNull(pageVO.getCustomerId(), "客户编号不能为空");
        PageResult<CrmContractDO> pageResult = contractService.getContractPageByCustomerId(pageVO);
        return success(BeanUtils.toBean(pageResult, CrmContractRespVO.class).setList(buildContractDetailList(pageResult.getList())));
    }

    @GetMapping("/page-by-business")
    @Operation(summary = "获得合同分页，基于指定商机")
    public CommonResult<PageResult<CrmContractRespVO>> getContractPageByBusiness(@Valid CrmContractPageReqVO pageVO) {
        Assert.notNull(pageVO.getBusinessId(), "商机编号不能为空");
        PageResult<CrmContractDO> pageResult = contractService.getContractPageByBusinessId(pageVO);
        return success(BeanUtils.toBean(pageResult, CrmContractRespVO.class).setList(buildContractDetailList(pageResult.getList())));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出合同 Excel")
    @PreAuthorize("@ss.hasPermission('crm:contract:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportContractExcel(@Valid CrmContractPageReqVO exportReqVO,
                                    HttpServletResponse response) throws IOException {
        PageResult<CrmContractDO> pageResult = contractService.getContractPage(exportReqVO, getLoginUserId());
        // 导出 Excel
        ExcelUtils.write(response, "合同.xls", "数据", CrmContractRespVO.class,
                BeanUtils.toBean(pageResult.getList(), CrmContractRespVO.class));
    }

    @PutMapping("/transfer")
    @Operation(summary = "合同转移")
    @PreAuthorize("@ss.hasPermission('crm:contract:update')")
    public CommonResult<Boolean> transferContract(@Valid @RequestBody CrmContractTransferReqVO reqVO) {
        contractService.transferContract(reqVO, getLoginUserId());
        return success(true);
    }

    @PutMapping("/submit")
    @Operation(summary = "提交合同审批")
    @PreAuthorize("@ss.hasPermission('crm:contract:update')")
    public CommonResult<Boolean> submitContract(@RequestParam("id") Long id) {
        contractService.submitContract(id, getLoginUserId());
        return success(true);
    }

    private List<CrmContractRespVO> buildContractDetailList(List<CrmContractDO> contractList) {
        if (CollUtil.isEmpty(contractList)) {
            return Collections.emptyList();
        }
        // 1.1 获取客户列表
        Map<Long, CrmCustomerDO> customerMap = customerService.getCustomerMap(
                convertSet(contractList, CrmContractDO::getCustomerId));
        // 1.2 获取创建人、负责人列表
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertListByFlatMap(contractList,
                contact -> Stream.of(NumberUtils.parseLong(contact.getCreator()), contact.getOwnerUserId())));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(userMap.values(), AdminUserRespDTO::getDeptId));
        // 1.3 获取联系人
        Map<Long, CrmContactDO> contactMap = convertMap(contactService.getContactList(convertSet(contractList,
                CrmContractDO::getSignContactId)), CrmContactDO::getId);
        // 1.4 获取商机
        Map<Long, CrmBusinessDO> businessMap = businessService.getBusinessMap(
                convertSet(contractList, CrmContractDO::getBusinessId));
        // 1.5 获得已回款金额
        Map<Long, BigDecimal> receivablePriceMap = receivableService.getReceivablePriceMapByContractId(
                convertSet(contractList, CrmContractDO::getId));
        // 2. 拼接数据
        return BeanUtils.toBean(contractList, CrmContractRespVO.class, contractVO -> {
            // 2.1 设置客户信息
            findAndThen(customerMap, contractVO.getCustomerId(), customer -> contractVO.setCustomerName(customer.getName()));
            // 2.2 设置用户信息
            findAndThen(userMap, Long.parseLong(contractVO.getCreator()), user -> contractVO.setCreatorName(user.getNickname()));
            MapUtils.findAndThen(userMap, contractVO.getOwnerUserId(), user -> {
                contractVO.setOwnerUserName(user.getNickname());
                MapUtils.findAndThen(deptMap, user.getDeptId(), dept -> contractVO.setOwnerUserDeptName(dept.getName()));
            });
            findAndThen(userMap, contractVO.getSignUserId(), user -> contractVO.setSignUserName(user.getNickname()));
            // 2.3 设置联系人信息
            findAndThen(contactMap, contractVO.getSignContactId(), contact -> contractVO.setSignContactName(contact.getName()));
            // 2.4 设置商机信息
            findAndThen(businessMap, contractVO.getBusinessId(), business -> contractVO.setBusinessName(business.getName()));
            // 2.5 设置已回款金额
            contractVO.setTotalReceivablePrice(receivablePriceMap.getOrDefault(contractVO.getId(), BigDecimal.ZERO));
        });
    }

    @GetMapping("/audit-count")
    @Operation(summary = "获得待审核合同数量")
    @PreAuthorize("@ss.hasPermission('crm:contract:query')")
    public CommonResult<Long> getAuditContractCount() {
        return success(contractService.getAuditContractCount(getLoginUserId()));
    }

    @GetMapping("/remind-count")
    @Operation(summary = "获得即将到期（提醒）的合同数量")
    @PreAuthorize("@ss.hasPermission('crm:contract:query')")
    public CommonResult<Long> getRemindContractCount() {
        return success(contractService.getRemindContractCount(getLoginUserId()));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得合同精简列表", description = "只包含的合同，主要用于前端的下拉选项")
    @Parameter(name = "customerId", description = "客户编号", required = true)
    @PreAuthorize("@ss.hasPermission('crm:contract:query')")
    public CommonResult<List<CrmContractRespVO>> getContractSimpleList(@RequestParam("customerId") Long customerId) {
        CrmContractPageReqVO pageReqVO = new CrmContractPageReqVO().setCustomerId(customerId);
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE); // 不分页
        PageResult<CrmContractDO> pageResult = contractService.getContractPageByCustomerId(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(Collections.emptyList());
        }
        // 拼接数据
        Map<Long, BigDecimal> receivablePriceMap = receivableService.getReceivablePriceMapByContractId(
                convertSet(pageResult.getList(), CrmContractDO::getId));
        return success(convertList(pageResult.getList(), contract -> new CrmContractRespVO() // 只返回 id、name 等精简字段
                .setId(contract.getId()).setName(contract.getName()).setAuditStatus(contract.getAuditStatus())
                .setTotalPrice(contract.getTotalPrice())
                .setTotalReceivablePrice(receivablePriceMap.getOrDefault(contract.getId(), BigDecimal.ZERO))));
    }

}
