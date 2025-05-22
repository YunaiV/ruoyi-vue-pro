package cn.iocoder.yudao.module.crm.controller.admin.receivable;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.plan.CrmReceivablePlanPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.plan.CrmReceivablePlanRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.plan.CrmReceivablePlanSaveReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.receivable.CrmReceivableRespVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.CrmContractDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.CrmReceivableDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.CrmReceivablePlanDO;
import cn.iocoder.yudao.module.crm.service.contract.CrmContractService;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
import cn.iocoder.yudao.module.crm.service.receivable.CrmReceivablePlanService;
import cn.iocoder.yudao.module.crm.service.receivable.CrmReceivableService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
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
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - CRM 回款计划")
@RestController
@RequestMapping("/crm/receivable-plan")
@Validated
public class CrmReceivablePlanController {

    @Resource
    private CrmReceivablePlanService receivablePlanService;
    @Resource
    private CrmReceivableService receivableService;
    @Resource
    private CrmContractService contractService;
    @Resource
    private CrmCustomerService customerService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建回款计划")
    @PreAuthorize("@ss.hasPermission('crm:receivable-plan:create')")
    public CommonResult<Long> createReceivablePlan(@Valid @RequestBody CrmReceivablePlanSaveReqVO createReqVO) {
        return success(receivablePlanService.createReceivablePlan(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新回款计划")
    @PreAuthorize("@ss.hasPermission('crm:receivable-plan:update')")
    public CommonResult<Boolean> updateReceivablePlan(@Valid @RequestBody CrmReceivablePlanSaveReqVO updateReqVO) {
        receivablePlanService.updateReceivablePlan(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除回款计划")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('crm:receivable-plan:delete')")
    public CommonResult<Boolean> deleteReceivablePlan(@RequestParam("id") Long id) {
        receivablePlanService.deleteReceivablePlan(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得回款计划")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:receivable-plan:query')")
    public CommonResult<CrmReceivablePlanRespVO> getReceivablePlan(@RequestParam("id") Long id) {
        CrmReceivablePlanDO receivablePlan = receivablePlanService.getReceivablePlan(id);
        return success(buildReceivablePlanDetail(receivablePlan));
    }

    private CrmReceivablePlanRespVO buildReceivablePlanDetail(CrmReceivablePlanDO receivablePlan) {
        if (receivablePlan == null) {
            return null;
        }
        return buildReceivableDetailList(Collections.singletonList(receivablePlan)).get(0);
    }

    @GetMapping("/page")
    @Operation(summary = "获得回款计划分页")
    @PreAuthorize("@ss.hasPermission('crm:receivable-plan:query')")
    public CommonResult<PageResult<CrmReceivablePlanRespVO>> getReceivablePlanPage(@Valid CrmReceivablePlanPageReqVO pageReqVO) {
        PageResult<CrmReceivablePlanDO> pageResult = receivablePlanService.getReceivablePlanPage(pageReqVO, getLoginUserId());
        return success(new PageResult<>(buildReceivableDetailList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/page-by-customer")
    @Operation(summary = "获得回款计划分页，基于指定客户")
    public CommonResult<PageResult<CrmReceivablePlanRespVO>> getReceivablePlanPageByCustomer(@Valid CrmReceivablePlanPageReqVO pageReqVO) {
        Assert.notNull(pageReqVO.getCustomerId(), "客户编号不能为空");
        PageResult<CrmReceivablePlanDO> pageResult = receivablePlanService.getReceivablePlanPageByCustomerId(pageReqVO);
        return success(new PageResult<>(buildReceivableDetailList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出回款计划 Excel")
    @PreAuthorize("@ss.hasPermission('crm:receivable-plan:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportReceivablePlanExcel(@Valid CrmReceivablePlanPageReqVO exportReqVO,
                                          HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PAGE_SIZE_NONE);
        List<CrmReceivablePlanDO> list = receivablePlanService.getReceivablePlanPage(exportReqVO, getLoginUserId()).getList();
        // 导出 Excel
        ExcelUtils.write(response, "回款计划.xls", "数据", CrmReceivablePlanRespVO.class,
                buildReceivableDetailList(list));
    }

    private List<CrmReceivablePlanRespVO> buildReceivableDetailList(List<CrmReceivablePlanDO> receivablePlanList) {
        if (CollUtil.isEmpty(receivablePlanList)) {
            return Collections.emptyList();
        }
        // 1.1 获取客户 Map
        Map<Long, CrmCustomerDO> customerMap = customerService.getCustomerMap(
                convertSet(receivablePlanList, CrmReceivablePlanDO::getCustomerId));
        // 1.2 获取创建人、负责人列表
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertListByFlatMap(receivablePlanList,
                contact -> Stream.of(NumberUtils.parseLong(contact.getCreator()), contact.getOwnerUserId())));
        // 1.3 获得合同 Map
        Map<Long, CrmContractDO> contractMap = contractService.getContractMap(
                convertSet(receivablePlanList, CrmReceivablePlanDO::getContractId));
        // 1.4 获得回款 Map
        Map<Long, CrmReceivableDO> receivableMap = receivableService.getReceivableMap(
                convertSet(receivablePlanList, CrmReceivablePlanDO::getReceivableId));
        // 2. 拼接数据
        return BeanUtils.toBean(receivablePlanList, CrmReceivablePlanRespVO.class, (receivablePlanVO) -> {
            // 2.1 拼接客户信息
            findAndThen(customerMap, receivablePlanVO.getCustomerId(), customer -> receivablePlanVO.setCustomerName(customer.getName()));
            // 2.2 拼接用户信息
            findAndThen(userMap, receivablePlanVO.getOwnerUserId(), user -> receivablePlanVO.setOwnerUserName(user.getNickname()));
            findAndThen(userMap, Long.parseLong(receivablePlanVO.getCreator()), user -> receivablePlanVO.setCreatorName(user.getNickname()));
            // 2.3 拼接合同信息
            findAndThen(contractMap, receivablePlanVO.getContractId(), contract -> receivablePlanVO.setContractNo(contract.getNo()));
            // 2.4 拼接回款信息
            receivablePlanVO.setReceivable(BeanUtils.toBean(receivableMap.get(receivablePlanVO.getReceivableId()), CrmReceivableRespVO.class));
        });
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得回款计划精简列表", description = "获得回款计划精简列表，主要用于前端的下拉选项")
    @Parameters({
            @Parameter(name = "customerId", description = "客户编号", required = true),
            @Parameter(name = "contractId", description = "合同编号", required = true)
    })
    @PreAuthorize("@ss.hasPermission('crm:receivable-plan:query')")
    public CommonResult<List<CrmReceivablePlanRespVO>> getReceivablePlanSimpleList(@RequestParam("customerId") Long customerId,
                                                                                   @RequestParam("contractId") Long contractId) {
        CrmReceivablePlanPageReqVO pageReqVO = new CrmReceivablePlanPageReqVO().setCustomerId(customerId).setContractId(contractId);
        pageReqVO.setPageNo(PAGE_SIZE_NONE);
        PageResult<CrmReceivablePlanDO> pageResult = receivablePlanService.getReceivablePlanPageByCustomerId(pageReqVO);
        return success(convertList(pageResult.getList(), receivablePlan -> new CrmReceivablePlanRespVO() // 只返回 id、period 等信息
                .setId(receivablePlan.getId()).setPeriod(receivablePlan.getPeriod()).setReceivableId(receivablePlan.getReceivableId())
                .setPrice(receivablePlan.getPrice()).setReturnType(receivablePlan.getReturnType())));
    }

    @GetMapping("/remind-count")
    @Operation(summary = "获得待回款提醒数量")
    @PreAuthorize("@ss.hasPermission('crm:receivable-plan:query')")
    public CommonResult<Long> getReceivablePlanRemindCount() {
        return success(receivablePlanService.getReceivablePlanRemindCount(getLoginUserId()));
    }

}
