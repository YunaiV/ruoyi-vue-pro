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
import cn.iocoder.yudao.module.fms.controller.admin.finance.vo.receipt.FmsFinanceReceiptPageReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.finance.vo.receipt.FmsFinanceReceiptRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.finance.vo.receipt.FmsFinanceReceiptSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.finance.FmsFinanceReceiptDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.finance.FmsFinanceReceiptItemDO;
import cn.iocoder.yudao.module.fms.service.finance.FmsFinanceReceiptService;
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

@Tag(name = "管理后台 - ERP 收款单")
@RestController
@RequestMapping("/erp/finance-receipt")
@Validated
public class FmsFinanceReceiptController {

    @Resource
    private FmsFinanceReceiptService financeReceiptService;
//    @Resource
//    private ErpCustomerService customerService;
//    @Resource
//    private FmsAccountService accountService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建收款单")
    @PreAuthorize("@ss.hasPermission('erp:finance-receipt:create')")
    public CommonResult<Long> createFinanceReceipt(@Valid @RequestBody FmsFinanceReceiptSaveReqVO createReqVO) {
        return success(financeReceiptService.createFinanceReceipt(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新收款单")
    @PreAuthorize("@ss.hasPermission('erp:finance-receipt:update')")
    public CommonResult<Boolean> updateFinanceReceipt(@Valid @RequestBody FmsFinanceReceiptSaveReqVO updateReqVO) {
        financeReceiptService.updateFinanceReceipt(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新收款单的状态")
    @PreAuthorize("@ss.hasPermission('erp:finance-receipt:update-status')")
    public CommonResult<Boolean> updateFinanceReceiptStatus(@RequestParam("id") Long id,
                                                            @RequestParam("status") Integer status) {
        financeReceiptService.updateFinanceReceiptStatus(id, status);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除收款单")
    @Parameter(name = "ids", description = "编号数组", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance-receipt:delete')")
    public CommonResult<Boolean> deleteFinanceReceipt(@RequestParam("ids") List<Long> ids) {
        financeReceiptService.deleteFinanceReceipt(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得收款单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:finance-receipt:query')")
    public CommonResult<FmsFinanceReceiptRespVO> getFinanceReceipt(@RequestParam("id") Long id) {
        FmsFinanceReceiptDO receipt = financeReceiptService.getFinanceReceipt(id);
        if (receipt == null) {
            return success(null);
        }
        List<FmsFinanceReceiptItemDO> receiptItemList = financeReceiptService.getFinanceReceiptItemListByReceiptId(id);
        return success(BeanUtils.toBean(receipt, FmsFinanceReceiptRespVO.class, financeReceiptVO ->
            financeReceiptVO.setItems(BeanUtils.toBean(receiptItemList, FmsFinanceReceiptRespVO.Item.class))));
    }

    @GetMapping("/page")
    @Operation(summary = "获得收款单分页")
    @PreAuthorize("@ss.hasPermission('erp:finance-receipt:query')")
    public CommonResult<PageResult<FmsFinanceReceiptRespVO>> getFinanceReceiptPage(@Valid FmsFinanceReceiptPageReqVO pageReqVO) {
        PageResult<FmsFinanceReceiptDO> pageResult = financeReceiptService.getFinanceReceiptPage(pageReqVO);
        return success(buildFinanceReceiptVOPageResult(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出收款单 Excel")
    @PreAuthorize("@ss.hasPermission('erp:finance-receipt:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportFinanceReceiptExcel(@Valid FmsFinanceReceiptPageReqVO pageReqVO,
                                          HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<FmsFinanceReceiptRespVO> list = buildFinanceReceiptVOPageResult(financeReceiptService.getFinanceReceiptPage(pageReqVO)).getList();
        // 导出 Excel
        ExcelUtils.write(response, "收款单.xls", "数据", FmsFinanceReceiptRespVO.class, list);
    }

    private PageResult<FmsFinanceReceiptRespVO> buildFinanceReceiptVOPageResult(PageResult<FmsFinanceReceiptDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        // 1.1 收款项
        List<FmsFinanceReceiptItemDO> receiptItemList = financeReceiptService.getFinanceReceiptItemListByReceiptIds(
            convertSet(pageResult.getList(), FmsFinanceReceiptDO::getId));
        Map<Long, List<FmsFinanceReceiptItemDO>> financeReceiptItemMap = convertMultiMap(receiptItemList, FmsFinanceReceiptItemDO::getReceiptId);
        // 1.2 客户信息
//        Map<Long, ErpCustomerDO> customerMap = customerService.getCustomerMap(
//                convertSet(pageResult.getList(), FmsFinanceReceiptDO::getCustomerId));
//        // 1.3 结算账户信息
//        Map<Long, FmsAccountDO> accountMap = accountService.getAccountMap(
//                convertSet(pageResult.getList(), FmsFinanceReceiptDO::getAccountId));
        // 1.4 管理员信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertListByFlatMap(pageResult.getList(),
            contact -> Stream.of(NumberUtils.parseLong(contact.getCreator()), contact.getFinanceUserId())));
        // 2. 开始拼接
        return BeanUtils.toBean(pageResult, FmsFinanceReceiptRespVO.class, receipt -> {
            receipt.setItems(BeanUtils.toBean(financeReceiptItemMap.get(receipt.getId()), FmsFinanceReceiptRespVO.Item.class));
//            MapUtils.findAndThen(customerMap, receipt.getCustomerId(), customer -> receipt.setCustomerName(customer.getName()));
//            MapUtils.findAndThen(accountMap, receipt.getAccountId(), account -> receipt.setAccountName(account.getName()));
            MapUtils.findAndThen(userMap, Long.parseLong(receipt.getCreator()), user -> receipt.setCreatorName(user.getNickname()));
            MapUtils.findAndThen(userMap, receipt.getFinanceUserId(), user -> receipt.setFinanceUserName(user.getNickname()));
        });
    }

}
