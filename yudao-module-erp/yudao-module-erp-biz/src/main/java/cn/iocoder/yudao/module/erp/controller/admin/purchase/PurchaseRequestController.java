package cn.iocoder.yudao.module.erp.controller.admin.purchase;

import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.PurchaseRequestPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.PurchaseRequestRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.PurchaseRequestSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.PurchaseRequestDO;
import cn.iocoder.yudao.module.erp.service.purchase.PurchaseRequestService;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.*;
import jakarta.servlet.http.*;
import java.util.*;
import java.io.IOException;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;


@Tag(name = "管理后台 - ERP采购申请单")
@RestController
@RequestMapping("/erp/purchase-request")
@Validated
public class PurchaseRequestController {

    @Resource
    private PurchaseRequestService purchaseRequestService;

    @PostMapping("/create")
    @Operation(summary = "创建ERP采购申请单")
    @PreAuthorize("@ss.hasPermission('erp:purchase-request:create')")
    public CommonResult<Long> createPurchaseRequest(@Valid @RequestBody PurchaseRequestSaveReqVO createReqVO) {
        return success(purchaseRequestService.createPurchaseRequest(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新ERP采购申请单")
    @PreAuthorize("@ss.hasPermission('erp:purchase-request:update')")
    public CommonResult<Boolean> updatePurchaseRequest(@Valid @RequestBody PurchaseRequestSaveReqVO updateReqVO) {
        purchaseRequestService.updatePurchaseRequest(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除ERP采购申请单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:purchase-request:delete')")
    public CommonResult<Boolean> deletePurchaseRequest(@RequestParam("id") Long id) {
        purchaseRequestService.deletePurchaseRequest(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得ERP采购申请单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:purchase-request:query')")
    public CommonResult<PurchaseRequestRespVO> getPurchaseRequest(@RequestParam("id") Long id) {
        PurchaseRequestDO purchaseRequest = purchaseRequestService.getPurchaseRequest(id);
        return success(BeanUtils.toBean(purchaseRequest, PurchaseRequestRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得ERP采购申请单分页")
    @PreAuthorize("@ss.hasPermission('erp:purchase-request:query')")
    public CommonResult<PageResult<PurchaseRequestRespVO>> getPurchaseRequestPage(@Valid PurchaseRequestPageReqVO pageReqVO) {
        PageResult<PurchaseRequestDO> pageResult = purchaseRequestService.getPurchaseRequestPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, PurchaseRequestRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出ERP采购申请单 Excel")
    @PreAuthorize("@ss.hasPermission('erp:purchase-request:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPurchaseRequestExcel(@Valid PurchaseRequestPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<PurchaseRequestDO> list = purchaseRequestService.getPurchaseRequestPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "ERP采购申请单.xls", "数据", PurchaseRequestRespVO.class,
                        BeanUtils.toBean(list, PurchaseRequestRespVO.class));
    }

}