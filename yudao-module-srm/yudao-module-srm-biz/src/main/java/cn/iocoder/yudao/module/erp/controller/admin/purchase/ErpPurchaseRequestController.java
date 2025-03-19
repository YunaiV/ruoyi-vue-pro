package cn.iocoder.yudao.module.erp.controller.admin.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.ErpPurchaseRequestPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.ErpPurchaseRequestRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.ErpPurchaseRequestSaveReqVO;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaseRequestService;
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

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
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
    @Resource
    private AdminUserApi adminUserApi;

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

    @PutMapping("/update-status")
    @Operation(summary = "更新采购申请单的状态")
    @PreAuthorize("@ss.hasPermission('erp:purchase-request:update-status')")
    public CommonResult<Boolean> updatePurchaseOrderStatus(@RequestParam("id") Long id,
                                                           @RequestParam("status") Integer status) {
        erpPurchaseRequestService.updatePurchaseRequestStatus(id, status);
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
        return success(buildPurchaseRequestVOPageResult(pageResult));
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

    private PageResult<ErpPurchaseRequestRespVO> buildPurchaseRequestVOPageResult(PageResult<ErpPurchaseRequestDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        // 1.1 申请单项
        List<ErpPurchaseRequestItemsDO> purchaseRequestItemList = erpPurchaseRequestService.getPurchaseRequestItemListByOrderIds(
                convertSet(pageResult.getList(), ErpPurchaseRequestDO::getId));
        Map<Long, List<ErpPurchaseRequestItemsDO>> purchaseRequestItemMap = convertMultiMap(purchaseRequestItemList, ErpPurchaseRequestItemsDO::getRequestId);
        // 1.2 产品信息
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(purchaseRequestItemList, ErpPurchaseRequestItemsDO::getProductId));
        // 1.3 申请人信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(pageResult.getList(), purchaseRequest -> Long.parseLong(purchaseRequest.getApplicant())));
        // 2. 开始拼接
        return BeanUtils.toBean(pageResult, ErpPurchaseRequestRespVO.class, purchaseRequest -> {
            purchaseRequest.setItems(BeanUtils.toBean(purchaseRequestItemMap.get(purchaseRequest.getId()), ErpPurchaseRequestRespVO.Item.class,
                    item -> MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName())
                            .setProductBarCode(product.getBarCode()).setProductUnitName(product.getUnitName()))));
            purchaseRequest.setProductNames(CollUtil.join(purchaseRequest.getItems(), "，", ErpPurchaseRequestRespVO.Item::getProductName));
            purchaseRequest.setTotalCount(CollUtil.isEmpty(purchaseRequest.getItems())?0:purchaseRequest.getItems().stream().mapToInt(ErpPurchaseRequestRespVO.Item::getCount).sum());
            MapUtils.findAndThen(userMap, Long.parseLong(purchaseRequest.getApplicant()), user -> purchaseRequest.setApplicantName(user.getNickname()));
        });
    }

}