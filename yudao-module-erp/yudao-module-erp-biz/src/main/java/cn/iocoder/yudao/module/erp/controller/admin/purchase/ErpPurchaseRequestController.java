package cn.iocoder.yudao.module.erp.controller.admin.purchase;

import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.ErpPurchaseRequestPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.ErpPurchaseRequestRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.ErpPurchaseRequestSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaseRequestService;
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
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;


/**
 * @author Administrator
 */
@Tag(name = "管理后台 - ERP采购申请单")
@RestController
@RequestMapping("/erp/purchase-request")
@Validated
public class ErpPurchaseRequestController {

    @Resource
    private ErpPurchaseRequestService erpPurchaseRequestService;
    @Resource
    private ErpProductService productService;

    @PostMapping("/create")
    @Operation(summary = "创建ERP采购申请单")
    @PreAuthorize("@ss.hasPermission('erp:purchase-request:create')")
    public CommonResult<Long> createPurchaseRequest(@Valid @RequestBody ErpPurchaseRequestSaveReqVO createReqVO) {
        return success(erpPurchaseRequestService.createPurchaseRequest(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新ERP采购申请单")
    @PreAuthorize("@ss.hasPermission('erp:purchase-request:update')")
    public CommonResult<Boolean> updatePurchaseRequest(@Valid @RequestBody ErpPurchaseRequestSaveReqVO updateReqVO) {
        erpPurchaseRequestService.updatePurchaseRequest(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除ERP采购申请单")
    @Parameter(name = "id", description = "编号数组", required = true)
    @PreAuthorize("@ss.hasPermission('erp:purchase-request:delete')")
    public CommonResult<Boolean> deletePurchaseRequest(@RequestParam("ids") List<Long> ids) {
        erpPurchaseRequestService.deletePurchaseRequest(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得ERP采购申请单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:purchase-request:query')")
    public CommonResult<ErpPurchaseRequestRespVO> getPurchaseRequest(@RequestParam("id") Long id) {
        ErpPurchaseRequestDO purchaseRequest = erpPurchaseRequestService.getPurchaseRequest(id);
        if (purchaseRequest == null) {
            return success(null);
        }
        List<ErpPurchaseRequestItemsDO> purchaseRequestItemsList = erpPurchaseRequestService.getPurchaseRequestItemListByOrderId(id);
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(purchaseRequestItemsList, ErpPurchaseRequestItemsDO::getProductId));
        return success(BeanUtils.toBean(purchaseRequest, ErpPurchaseRequestRespVO.class, purchaseOrderVO ->
                purchaseOrderVO.setItems(BeanUtils.toBean(purchaseRequestItemsList, ErpPurchaseRequestRespVO.Item.class, item -> {
                    MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName())
                            .setProductBarCode(product.getBarCode()).setProductUnitName(product.getUnitName()));
                }))));
    }

    @GetMapping("/page")
    @Operation(summary = "获得ERP采购申请单分页")
    @PreAuthorize("@ss.hasPermission('erp:purchase-request:query')")
    public CommonResult<PageResult<ErpPurchaseRequestRespVO>> getPurchaseRequestPage(@Valid ErpPurchaseRequestPageReqVO pageReqVO) {
        PageResult<ErpPurchaseRequestDO> pageResult = erpPurchaseRequestService.getPurchaseRequestPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ErpPurchaseRequestRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出ERP采购申请单 Excel")
    @PreAuthorize("@ss.hasPermission('erp:purchase-request:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPurchaseRequestExcel(@Valid ErpPurchaseRequestPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpPurchaseRequestDO> list = erpPurchaseRequestService.getPurchaseRequestPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "ERP采购申请单.xls", "数据", ErpPurchaseRequestRespVO.class,
                        BeanUtils.toBean(list, ErpPurchaseRequestRespVO.class));
    }

}