package cn.iocoder.yudao.module.crm.controller.admin.receivable;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.plan.CrmReceivablePlanCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.plan.CrmReceivablePlanPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.plan.CrmReceivablePlanRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.plan.CrmReceivablePlanUpdateReqVO;
import cn.iocoder.yudao.module.crm.convert.receivable.CrmReceivablePlanConvert;
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
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertListByFlatMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

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
    public CommonResult<Long> createReceivablePlan(@Valid @RequestBody CrmReceivablePlanCreateReqVO createReqVO) {
        return success(receivablePlanService.createReceivablePlan(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新回款计划")
    @PreAuthorize("@ss.hasPermission('crm:receivable-plan:update')")
    public CommonResult<Boolean> updateReceivablePlan(@Valid @RequestBody CrmReceivablePlanUpdateReqVO updateReqVO) {
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
        return success(CrmReceivablePlanConvert.INSTANCE.convert(receivablePlan));
    }

    @GetMapping("/page")
    @Operation(summary = "获得回款计划分页")
    @PreAuthorize("@ss.hasPermission('crm:receivable-plan:query')")
    public CommonResult<PageResult<CrmReceivablePlanRespVO>> getReceivablePlanPage(@Valid CrmReceivablePlanPageReqVO pageReqVO) {
        PageResult<CrmReceivablePlanDO> pageResult = receivablePlanService.getReceivablePlanPage(pageReqVO);
        return success(convertDetailReceivablePlanPage(pageResult));
    }

    @GetMapping("/page-by-customer")
    @Operation(summary = "获得回款计划分页，基于指定客户")
    public CommonResult<PageResult<CrmReceivablePlanRespVO>> getReceivablePlanPageByCustomer(@Valid CrmReceivablePlanPageReqVO pageReqVO) {
        Assert.notNull(pageReqVO.getCustomerId(), "客户编号不能为空");
        PageResult<CrmReceivablePlanDO> pageResult = receivablePlanService.getReceivablePlanPageByCustomer(pageReqVO);
        return success(convertDetailReceivablePlanPage(pageResult));
    }

    // TODO 芋艿：后面在优化导出
    @GetMapping("/export-excel")
    @Operation(summary = "导出回款计划 Excel")
    @PreAuthorize("@ss.hasPermission('crm:receivable-plan:export')")
    @OperateLog(type = EXPORT)
    public void exportReceivablePlanExcel(@Valid CrmReceivablePlanPageReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        PageResult<CrmReceivablePlanDO> pageResult = receivablePlanService.getReceivablePlanPage(exportReqVO);
        // 导出 Excel
        ExcelUtils.write(response, "回款计划.xls", "数据", CrmReceivablePlanRespVO.class,
                convertDetailReceivablePlanPage(pageResult).getList());
    }

    /**
     * 转换成详细的回款计划分页，即读取关联信息
     *
     * @param pageResult 回款计划分页
     * @return 详细的回款计划分页
     */
    private PageResult<CrmReceivablePlanRespVO> convertDetailReceivablePlanPage(PageResult<CrmReceivablePlanDO> pageResult) {
        List<CrmReceivablePlanDO> receivablePlanList = pageResult.getList();
        if (CollUtil.isEmpty(receivablePlanList)) {
            return PageResult.empty(pageResult.getTotal());
        }
        // 1. 获取客户列表
        List<CrmCustomerDO> customerList = customerService.getCustomerList(
                convertSet(receivablePlanList, CrmReceivablePlanDO::getCustomerId));
        // 2. 获取创建人、负责人列表
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertListByFlatMap(receivablePlanList,
                contact -> Stream.of(NumberUtils.parseLong(contact.getCreator()), contact.getOwnerUserId())));
        // 3. 获得合同列表
        List<CrmContractDO> contractList = contractService.getContractList(
                convertSet(receivablePlanList, CrmReceivablePlanDO::getContractId));
        // 4. 获得还款列表
        List<CrmReceivableDO> receivableList = receivableService.getReceivableList(
                convertSet(receivablePlanList, CrmReceivablePlanDO::getReceivableId));
        return CrmReceivablePlanConvert.INSTANCE.convertPage(pageResult, userMap, customerList, contractList, receivableList);
    }

}
