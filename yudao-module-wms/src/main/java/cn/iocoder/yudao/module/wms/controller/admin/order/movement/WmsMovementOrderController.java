package cn.iocoder.yudao.module.wms.controller.admin.order.movement;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import cn.iocoder.yudao.module.wms.controller.admin.order.movement.vo.detail.WmsMovementOrderDetailRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.order.movement.vo.order.WmsMovementOrderPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.order.movement.vo.order.WmsMovementOrderRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.order.movement.vo.order.WmsMovementOrderSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemSkuDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.movement.WmsMovementOrderDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.movement.WmsMovementOrderDetailDO;
import cn.iocoder.yudao.module.wms.service.md.item.WmsItemService;
import cn.iocoder.yudao.module.wms.service.md.item.WmsItemSkuService;
import cn.iocoder.yudao.module.wms.service.md.warehouse.WmsWarehouseService;
import cn.iocoder.yudao.module.wms.service.order.movement.WmsMovementOrderDetailService;
import cn.iocoder.yudao.module.wms.service.order.movement.WmsMovementOrderService;
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
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSetByFlatMap;

@Tag(name = "管理后台 - WMS 移库单")
@RestController
@RequestMapping("/wms/movement-order")
@Validated
public class WmsMovementOrderController {

    @Resource
    private WmsMovementOrderService movementOrderService;
    @Resource
    private WmsMovementOrderDetailService movementOrderDetailService;
    @Resource
    private WmsWarehouseService warehouseService;
    @Resource
    private WmsItemService itemService;
    @Resource
    private WmsItemSkuService itemSkuService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建移库单")
    @PreAuthorize("@ss.hasPermission('wms:movement-order:create')")
    public CommonResult<Long> createMovementOrder(@Valid @RequestBody WmsMovementOrderSaveReqVO createReqVO) {
        return success(movementOrderService.createMovementOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新移库单")
    @PreAuthorize("@ss.hasPermission('wms:movement-order:update')")
    public CommonResult<Boolean> updateMovementOrder(@Valid @RequestBody WmsMovementOrderSaveReqVO updateReqVO) {
        movementOrderService.updateMovementOrder(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除移库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:movement-order:delete')")
    public CommonResult<Boolean> deleteMovementOrder(@RequestParam("id") Long id) {
        movementOrderService.deleteMovementOrder(id);
        return success(true);
    }

    @PutMapping("/complete")
    @Operation(summary = "完成移库")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:movement-order:complete')")
    public CommonResult<Boolean> completeMovementOrder(@RequestParam("id") Long id) {
        movementOrderService.completeMovementOrder(id);
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "作废移库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:movement-order:cancel')")
    public CommonResult<Boolean> cancelMovementOrder(@RequestParam("id") Long id) {
        movementOrderService.cancelMovementOrder(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得移库单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:movement-order:query')")
    public CommonResult<WmsMovementOrderRespVO> getMovementOrder(@RequestParam("id") Long id) {
        WmsMovementOrderDO order = movementOrderService.getMovementOrder(id);
        if (order == null) {
            return success(null);
        }
        // 获得移库单的明细列表
        List<WmsMovementOrderDetailDO> detailList = movementOrderDetailService.getMovementOrderDetailList(id);
        // 拼接结果返回
        WmsMovementOrderRespVO respVO = buildMovementOrderRespVO(order)
                .setDetails(buildMovementOrderDetailRespVOList(detailList));
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得移库单分页")
    @PreAuthorize("@ss.hasPermission('wms:movement-order:query')")
    public CommonResult<PageResult<WmsMovementOrderRespVO>> getMovementOrderPage(
            @Valid WmsMovementOrderPageReqVO pageReqVO) {
        PageResult<WmsMovementOrderDO> pageResult = movementOrderService.getMovementOrderPage(pageReqVO);
        return success(new PageResult<>(buildMovementOrderRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出移库单 Excel")
    @PreAuthorize("@ss.hasPermission('wms:movement-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportMovementOrderExcel(@Valid WmsMovementOrderPageReqVO pageReqVO,
                                         HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsMovementOrderDO> list = movementOrderService.getMovementOrderPage(pageReqVO).getList();
        ExcelUtils.write(response, "移库单.xls", "数据", WmsMovementOrderRespVO.class,
                buildMovementOrderRespVOList(list));
    }

    // ==================== 拼接 VO ====================

    private WmsMovementOrderRespVO buildMovementOrderRespVO(WmsMovementOrderDO order) {
        if (order == null) {
            return null;
        }
        List<WmsMovementOrderRespVO> list = buildMovementOrderRespVOList(Collections.singletonList(order));
        return CollUtil.getFirst(list);
    }

    private List<WmsMovementOrderRespVO> buildMovementOrderRespVOList(List<WmsMovementOrderDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 获取相关的仓库、用户等数据
        Map<Long, WmsWarehouseDO> warehouseMap = warehouseService.getWarehouseMap(convertSetByFlatMap(list,
                order -> Stream.of(order.getSourceWarehouseId(), order.getTargetWarehouseId())));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSetByFlatMap(list,
                order -> Stream.of(parseUserId(order.getCreator()), parseUserId(order.getUpdater()))));
        // 拼接数据
        return BeanUtils.toBean(list, WmsMovementOrderRespVO.class, vo -> {
            MapUtils.findAndThen(warehouseMap, vo.getSourceWarehouseId(),
                    warehouse -> vo.setSourceWarehouseName(warehouse.getName()));
            MapUtils.findAndThen(warehouseMap, vo.getTargetWarehouseId(),
                    warehouse -> vo.setTargetWarehouseName(warehouse.getName()));
            MapUtils.findAndThen(userMap, parseUserId(vo.getCreator()), user -> vo.setCreatorName(user.getNickname()));
            MapUtils.findAndThen(userMap, parseUserId(vo.getUpdater()), user -> vo.setUpdaterName(user.getNickname()));
        });
    }

    private Long parseUserId(String userId) {
        return NumberUtil.parseLong(userId, null);
    }

    private List<WmsMovementOrderDetailRespVO> buildMovementOrderDetailRespVOList(List<WmsMovementOrderDetailDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 获取相关的商品、SKU、仓库等数据
        Map<Long, WmsItemSkuDO> skuMap = itemSkuService.getItemSkuMap(convertSet(list, WmsMovementOrderDetailDO::getSkuId));
        Map<Long, WmsItemDO> itemMap = itemService.getItemMap(convertSet(skuMap.values(), WmsItemSkuDO::getItemId));
        Map<Long, WmsWarehouseDO> warehouseMap = warehouseService.getWarehouseMap(convertSetByFlatMap(list,
                detail -> Stream.of(detail.getSourceWarehouseId(), detail.getTargetWarehouseId())));
        // 拼接数据
        return BeanUtils.toBean(list, WmsMovementOrderDetailRespVO.class, vo -> {
            MapUtils.findAndThen(skuMap, vo.getSkuId(), sku -> {
                vo.setSkuCode(sku.getCode()).setSkuName(sku.getName()).setItemId(sku.getItemId());
                MapUtils.findAndThen(itemMap, sku.getItemId(), item -> vo.setItemCode(item.getCode())
                        .setItemName(item.getName()).setUnit(item.getUnit()));
            });
            MapUtils.findAndThen(warehouseMap, vo.getSourceWarehouseId(),
                    warehouse -> vo.setSourceWarehouseName(warehouse.getName()));
            MapUtils.findAndThen(warehouseMap, vo.getTargetWarehouseId(),
                    warehouse -> vo.setTargetWarehouseName(warehouse.getName()));
        });
    }

}
