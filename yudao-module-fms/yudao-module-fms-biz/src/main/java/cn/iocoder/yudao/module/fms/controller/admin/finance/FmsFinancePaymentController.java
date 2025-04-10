package cn.iocoder.yudao.module.fms.controller.admin.finance;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.fms.controller.admin.finance.vo.payment.FmsFinancePaymentPageReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.finance.vo.payment.FmsFinancePaymentRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.finance.vo.payment.FmsFinancePaymentSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.finance.FmsAccountDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.finance.FmsFinancePaymentDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.finance.FmsFinancePaymentItemDO;
import cn.iocoder.yudao.module.fms.service.finance.FmsAccountService;
import cn.iocoder.yudao.module.fms.service.finance.FmsFinancePaymentService;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;

@Tag(name = "管理后台 - ERP 付款单")
@RestController
@RequestMapping("/fms/finance-payment")
@Validated
public class FmsFinancePaymentController {

    @Resource
    private FmsFinancePaymentService financePaymentService;

    @Resource
    private FmsAccountService accountService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建付款单")
    @PreAuthorize("@ss.hasPermission('fms:finance-payment:create')")
    public CommonResult<Long> createFinancePayment(@Valid @RequestBody FmsFinancePaymentSaveReqVO createReqVO) {
        return success(financePaymentService.createFinancePayment(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新付款单")
    @PreAuthorize("@ss.hasPermission('fms:finance-payment:update')")
    public CommonResult<Boolean> updateFinancePayment(@Valid @RequestBody FmsFinancePaymentSaveReqVO updateReqVO) {
        financePaymentService.updateFinancePayment(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新付款单的状态")
    @PreAuthorize("@ss.hasPermission('fms:finance-payment:update-status')")
    public CommonResult<Boolean> updateFinancePaymentStatus(@RequestParam("id") Long id,
                                                            @RequestParam("status") Integer status) {
        financePaymentService.updateFinancePaymentStatus(id, status);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除付款单")
    @Parameter(name = "ids", description = "编号数组", required = true)
    @PreAuthorize("@ss.hasPermission('fms:finance-payment:delete')")
    public CommonResult<Boolean> deleteFinancePayment(@RequestParam("ids") List<Long> ids) {
        financePaymentService.deleteFinancePayment(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得付款单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('fms:finance-payment:query')")
    public CommonResult<FmsFinancePaymentRespVO> getFinancePayment(@RequestParam("id") Long id) {
        FmsFinancePaymentDO payment = financePaymentService.getFinancePayment(id);
        if (payment == null) {
            return success(null);
        }
        List<FmsFinancePaymentItemDO> paymentItemList = financePaymentService.getFinancePaymentItemListByPaymentId(id);
        return success(BeanUtils.toBean(payment, FmsFinancePaymentRespVO.class, financePaymentVO ->
            financePaymentVO.setItems(BeanUtils.toBean(paymentItemList, FmsFinancePaymentRespVO.Item.class))));
    }

    @GetMapping("/page")
    @Operation(summary = "获得付款单分页")
    @PreAuthorize("@ss.hasPermission('fms:finance-payment:query')")
    public CommonResult<PageResult<FmsFinancePaymentRespVO>> getFinancePaymentPage(@Valid FmsFinancePaymentPageReqVO pageReqVO) {
        PageResult<FmsFinancePaymentDO> pageResult = financePaymentService.getFinancePaymentPage(pageReqVO);
        return success(buildFinancePaymentVOPageResult(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出付款单 Excel")
    @PreAuthorize("@ss.hasPermission('fms:finance-payment:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportFinancePaymentExcel(@Valid FmsFinancePaymentPageReqVO pageReqVO,
                                          HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<FmsFinancePaymentRespVO> list = buildFinancePaymentVOPageResult(financePaymentService.getFinancePaymentPage(pageReqVO)).getList();
        // 导出 Excel
        ExcelUtils.write(response, "付款单.xls", "数据", FmsFinancePaymentRespVO.class, list);
    }

    private PageResult<FmsFinancePaymentRespVO> buildFinancePaymentVOPageResult(PageResult<FmsFinancePaymentDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        // 1.1 付款项
        List<FmsFinancePaymentItemDO> paymentItemList = financePaymentService.getFinancePaymentItemListByPaymentIds(
            convertSet(pageResult.getList(), FmsFinancePaymentDO::getId));
        Map<Long, List<FmsFinancePaymentItemDO>> financePaymentItemMap = convertMultiMap(paymentItemList, FmsFinancePaymentItemDO::getPaymentId);
//        // 1.2 供应商信息
//        Map<Long, ErpSupplierDO> supplierMap = supplierService.getSupplierMap(
//                convertSet(pageResult.getList(), FmsFinancePaymentDO::getSupplierId));
        // 1.3 结算账户信息
        Map<Long, FmsAccountDO> accountMap = accountService.getAccountMap(
            convertSet(pageResult.getList(), FmsFinancePaymentDO::getAccountId));
        // 1.4 管理员信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertListByFlatMap(pageResult.getList(),
            contact -> Stream.of(NumberUtils.parseLong(contact.getCreator()), contact.getFinanceUserId())));
        // 2. 开始拼接
        return BeanUtils.toBean(pageResult, FmsFinancePaymentRespVO.class, payment -> {
            payment.setItems(BeanUtils.toBean(financePaymentItemMap.get(payment.getId()), FmsFinancePaymentRespVO.Item.class));
//            MapUtils.findAndThen(supplierMap, payment.getSupplierId(), supplier -> payment.setSupplierName(supplier.getName()));
            MapUtils.findAndThen(accountMap, payment.getAccountId(), account -> payment.setAccountName(account.getName()));
            MapUtils.findAndThen(userMap, Long.parseLong(payment.getCreator()), user -> payment.setCreatorName(user.getNickname()));
            MapUtils.findAndThen(userMap, payment.getFinanceUserId(), user -> payment.setFinanceUserName(user.getNickname()));
        });
    }

}
