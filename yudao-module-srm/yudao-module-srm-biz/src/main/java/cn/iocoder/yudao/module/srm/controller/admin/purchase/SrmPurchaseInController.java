package cn.iocoder.yudao.module.srm.controller.admin.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.idempotent.core.annotation.Idempotent;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.erp.api.stock.WmsWarehouseApi;
import cn.iocoder.yudao.module.erp.api.stock.dto.ErpWarehouseDTO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.SrmPurchaseInBaseRespVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.req.*;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInItemDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmSupplierDO;
import cn.iocoder.yudao.module.srm.service.purchase.SrmPurchaseInService;
import cn.iocoder.yudao.module.srm.service.purchase.SrmPurchaseOrderService;
import cn.iocoder.yudao.module.srm.service.purchase.SrmSupplierService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import cn.iocoder.yudao.module.system.api.utils.Validation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - ERP 采购入库")
@RestController
@RequestMapping("/erp/purchase-in")
@Validated
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SrmPurchaseInController {

    private final SrmPurchaseInService purchaseInService;
    private final SrmSupplierService supplierService;
    private final WmsWarehouseApi wmsWarehouseApi;
    private final AdminUserApi adminUserApi;
    private final DeptApi deptApi;
    private final ErpProductApi erpProductApi;
    @Autowired
    @Lazy
    SrmPurchaseOrderService srmPurchaseOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建采购入库")
    @Idempotent
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:create')")
    public CommonResult<Long> createPurchaseIn(@Valid @RequestBody SrmPurchaseInSaveReqVO createReqVO) {
        //给vo里面的项的source设置字符串a
        createReqVO.getItems().forEach(item -> item.setSource("WEB录入"));
        createReqVO.setInTime(LocalDateTime.now());
        return success(purchaseInService.createPurchaseIn(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新采购入库")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:update')")
    public CommonResult<Boolean> updatePurchaseIn(@Valid @RequestBody SrmPurchaseInSaveReqVO updateReqVO) {
        purchaseInService.updatePurchaseIn(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除采购入库")
    @Parameter(name = "ids", description = "编号数组", required = true)
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:delete')")
    public CommonResult<Boolean> deletePurchaseIn(@RequestParam("ids") List<Long> ids) {
        purchaseInService.deletePurchaseIn(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得采购入库")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:query')")
    public CommonResult<SrmPurchaseInBaseRespVO> getPurchaseIn(@RequestParam("id") Long id) {
        SrmPurchaseInDO purchaseIn = purchaseInService.getPurchaseIn(id);
        if (purchaseIn == null) {
            return success(null);
        }
        SrmPurchaseInBaseRespVO respVO = bindList(Collections.singletonList(purchaseIn)).get(0);
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得采购入库分页")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:query')")
    public CommonResult<PageResult<SrmPurchaseInBaseRespVO>> getPurchaseInPage(@Valid SrmPurchaseInPageReqVO pageReqVO) {
        PageResult<SrmPurchaseInDO> pageResult = purchaseInService.getPurchaseInPage(pageReqVO);
        List<SrmPurchaseInBaseRespVO> respVOS = bindList(pageResult.getList());
        return success(new PageResult<>(respVOS, pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出采购入库 Excel")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPurchaseInExcel(@Valid SrmPurchaseInPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<SrmPurchaseInDO> page = purchaseInService.getPurchaseInPage(pageReqVO);
        // 导出 Excel
        ExcelUtils.write(response, "采购入库.xls", "数据", SrmPurchaseInBaseRespVO.class, bindList(page.getList()));
    }

    @PutMapping("/submitAudit")
    @Operation(summary = "提交审核")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:submitAudit')")
    public CommonResult<Boolean> submitAudit(@Valid @RequestBody SrmPurchaseInSubmitReqVO vo) {
        purchaseInService.submitAudit(vo.getInIds().stream().distinct().toList());
        return success(true);
    }

    @PostMapping("/auditStatus")
    @Operation(summary = "审核/反审核")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:review')")
    public CommonResult<Boolean> reviewPurchaseRequest(@Validated(Validation.OnAudit.class) @RequestBody SrmPurchaseInAuditReqVO req) {
        purchaseInService.review(req);
        return success(true);
    }

    //切换付款状态方法(暂时)
    @PostMapping("/changePayStatus")
    @Operation(summary = "切换付款状态")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:changePayStatus')")
    public CommonResult<Boolean> changePayStatus(@Valid @RequestBody SrmPurchaseInPayReqVO vo) {
        //        purchaseInService.changePayStatus(reqVO.getInId(), reqVO.getPayStatus());
        purchaseInService.switchPayStatus(vo);
        return success(true);
    }
    //TODO 合并出库

    private List<SrmPurchaseInBaseRespVO> bindList(List<SrmPurchaseInDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1.1 入库项
        List<SrmPurchaseInItemDO> purchaseInItemList = purchaseInService.getPurchaseInItemListByInIds(convertSet(list, SrmPurchaseInDO::getId));
        Map<Long, List<SrmPurchaseInItemDO>> purchaseInItemMap = convertMultiMap(purchaseInItemList, SrmPurchaseInItemDO::getInId);
        // 1.2 产品信息
        Map<Long, ErpProductDTO> productMap = erpProductApi.getProductMap(convertSet(purchaseInItemList, SrmPurchaseInItemDO::getProductId));
        // 1.3 供应商信息
        Map<Long, SrmSupplierDO> supplierMap = supplierService.getSupplierMap(convertSet(list, SrmPurchaseInDO::getSupplierId));
        // 1.4 人员信息
        Set<Long> userIds = Stream.concat(list.stream().flatMap(orderDO -> Stream.of(orderDO.getAuditorId(),//审核者
                safeParseLong(orderDO.getCreator()), safeParseLong(orderDO.getUpdater()))), purchaseInItemList.stream()
                .flatMap(orderItemDO -> Stream.of(safeParseLong(orderItemDO.getCreator()), safeParseLong(orderItemDO.getUpdater()), orderItemDO.getApplicantId())))
            .distinct().filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        // 1.5 部门
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(purchaseInItemList, SrmPurchaseInItemDO::getApplicationDeptId));
        // 1.6 获取仓库信息
        Map<Long, ErpWarehouseDTO> warehouseMap = wmsWarehouseApi.getWarehouseMap(convertSet(purchaseInItemList, SrmPurchaseInItemDO::getWarehouseId));
        //1.7 订单项map orderItemId
        Map<Long, SrmPurchaseOrderDO> orderItemMap =
            srmPurchaseOrderService.getPurchaseOrderItemMap(purchaseInItemList.stream().map(SrmPurchaseInItemDO::getOrderItemId).collect(Collectors.toSet()));
        // 2. 开始拼接
        return BeanUtils.toBean(list, SrmPurchaseInBaseRespVO.class, purchaseIn -> {
            purchaseIn.setItems(BeanUtils.toBean(purchaseInItemMap.get(purchaseIn.getId()), SrmPurchaseInBaseRespVO.Item.class, item -> {
                //设置产品信息-带出相关字段
                MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProduct(product).setTotalVolume(
                            product.getLength() * product.getHeight() * product.getWidth() * Double.parseDouble(String.valueOf(item.getQty())))//总体积=数量*产品体积
                        .setTotalWeight(product.getWeight().setScale(2, RoundingMode.HALF_UP).longValue() * Double.parseDouble(String.valueOf(item.getQty())))
                    //总重量=数量*产品重量
                );
                // 设置仓库信息
                MapUtils.findAndThen(warehouseMap, item.getWarehouseId(), erpWarehouseDO -> item.setWarehouseName(erpWarehouseDO.getName()));
                //部门
                MapUtils.findAndThen(deptMap, item.getApplicationDeptId(), dept -> item.setApplicationDeptName(dept.getName()));
                //人员
                MapUtils.findAndThen(userMap, item.getApplicantId(), user -> item.setApplicantName(user.getNickname()));
                //订单的no
                MapUtils.findAndThen(orderItemMap, item.getOrderItemId(), order -> item.setOrderNo(order.getNo()));
            }));
            //            purchaseIn.setProductNames(CollUtil.join(purchaseIn.getItems(), "，", SrmPurchaseInBaseRespVO.Item::getProductName));
            //产品-带出相关字段
            MapUtils.findAndThen(supplierMap, purchaseIn.getSupplierId(), supplier -> purchaseIn.setSupplierName(supplier.getName()));

            //人员
            MapUtils.findAndThen(userMap, safeParseLong(purchaseIn.getCreator()), user -> purchaseIn.setCreator(user.getNickname()));
            MapUtils.findAndThen(userMap, safeParseLong(purchaseIn.getAuditor()), user -> purchaseIn.setAuditor(user.getNickname()));

        });
    }

    /**
     * 安全转换id为 Long
     *
     * @param value String类型的id
     * @return id
     */
    private Long safeParseLong(String value) {
        try {
            return Optional.ofNullable(value).map(Long::parseLong).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}