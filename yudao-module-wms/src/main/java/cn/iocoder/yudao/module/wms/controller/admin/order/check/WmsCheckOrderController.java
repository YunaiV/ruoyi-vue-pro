package cn.iocoder.yudao.module.wms.controller.admin.order.check;

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
import cn.iocoder.yudao.module.wms.controller.admin.order.check.vo.detail.WmsCheckOrderDetailRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.order.check.vo.order.WmsCheckOrderPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.order.check.vo.order.WmsCheckOrderRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.order.check.vo.order.WmsCheckOrderSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemSkuDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.check.WmsCheckOrderDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.check.WmsCheckOrderDetailDO;
import cn.iocoder.yudao.module.wms.service.md.item.WmsItemService;
import cn.iocoder.yudao.module.wms.service.md.item.WmsItemSkuService;
import cn.iocoder.yudao.module.wms.service.md.warehouse.WmsWarehouseService;
import cn.iocoder.yudao.module.wms.service.order.check.WmsCheckOrderDetailService;
import cn.iocoder.yudao.module.wms.service.order.check.WmsCheckOrderService;
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

@Tag(name = "管理后台 - WMS 盘库单")
@RestController
@RequestMapping("/wms/check-order")
@Validated
public class WmsCheckOrderController {

    @Resource
    private WmsCheckOrderService checkOrderService;
    @Resource
    private WmsCheckOrderDetailService checkOrderDetailService;
    @Resource
    private WmsWarehouseService warehouseService;
    @Resource
    private WmsItemService itemService;
    @Resource
    private WmsItemSkuService itemSkuService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建盘库单")
    @PreAuthorize("@ss.hasPermission('wms:check-order:create')")
    public CommonResult<Long> createCheckOrder(@Valid @RequestBody WmsCheckOrderSaveReqVO createReqVO) {
        return success(checkOrderService.createCheckOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新盘库单")
    @PreAuthorize("@ss.hasPermission('wms:check-order:update')")
    public CommonResult<Boolean> updateCheckOrder(@Valid @RequestBody WmsCheckOrderSaveReqVO updateReqVO) {
        checkOrderService.updateCheckOrder(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除盘库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:check-order:delete')")
    public CommonResult<Boolean> deleteCheckOrder(@RequestParam("id") Long id) {
        checkOrderService.deleteCheckOrder(id);
        return success(true);
    }

    @PutMapping("/complete")
    @Operation(summary = "完成盘库")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:check-order:complete')")
    public CommonResult<Boolean> completeCheckOrder(@RequestParam("id") Long id) {
        checkOrderService.completeCheckOrder(id);
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "作废盘库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:check-order:cancel')")
    public CommonResult<Boolean> cancelCheckOrder(@RequestParam("id") Long id) {
        checkOrderService.cancelCheckOrder(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得盘库单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:check-order:query')")
    public CommonResult<WmsCheckOrderRespVO> getCheckOrder(@RequestParam("id") Long id) {
        WmsCheckOrderDO order = checkOrderService.getCheckOrder(id);
        if (order == null) {
            return success(null);
        }
        // 获得盘库单的明细列表
        List<WmsCheckOrderDetailDO> detailList = checkOrderDetailService.getCheckOrderDetailList(id);
        // 拼接结果返回
        WmsCheckOrderRespVO respVO = buildCheckOrderRespVO(order)
                .setDetails(buildCheckOrderDetailRespVOList(detailList));
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得盘库单分页")
    @PreAuthorize("@ss.hasPermission('wms:check-order:query')")
    public CommonResult<PageResult<WmsCheckOrderRespVO>> getCheckOrderPage(@Valid WmsCheckOrderPageReqVO pageReqVO) {
        PageResult<WmsCheckOrderDO> pageResult = checkOrderService.getCheckOrderPage(pageReqVO);
        return success(new PageResult<>(buildCheckOrderRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出盘库单 Excel")
    @PreAuthorize("@ss.hasPermission('wms:check-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportCheckOrderExcel(@Valid WmsCheckOrderPageReqVO pageReqVO,
                                      HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsCheckOrderDO> list = checkOrderService.getCheckOrderPage(pageReqVO).getList();
        ExcelUtils.write(response, "盘库单.xls", "数据", WmsCheckOrderRespVO.class,
                buildCheckOrderRespVOList(list));
    }

    // ==================== 拼接 VO ====================

    private WmsCheckOrderRespVO buildCheckOrderRespVO(WmsCheckOrderDO order) {
        if (order == null) {
            return null;
        }
        List<WmsCheckOrderRespVO> list = buildCheckOrderRespVOList(Collections.singletonList(order));
        return CollUtil.getFirst(list);
    }

    private List<WmsCheckOrderRespVO> buildCheckOrderRespVOList(List<WmsCheckOrderDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 获取相关的仓库、用户等数据
        Map<Long, WmsWarehouseDO> warehouseMap = warehouseService.getWarehouseMap(convertSet(list, WmsCheckOrderDO::getWarehouseId));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSetByFlatMap(list,
                order -> Stream.of(parseUserId(order.getCreator()), parseUserId(order.getUpdater()))));
        // 拼接数据
        return BeanUtils.toBean(list, WmsCheckOrderRespVO.class, vo -> {
            MapUtils.findAndThen(warehouseMap, vo.getWarehouseId(), warehouse -> vo.setWarehouseName(warehouse.getName()));
            MapUtils.findAndThen(userMap, parseUserId(vo.getCreator()), user -> vo.setCreatorName(user.getNickname()));
            MapUtils.findAndThen(userMap, parseUserId(vo.getUpdater()), user -> vo.setUpdaterName(user.getNickname()));
        });
    }

    private Long parseUserId(String userId) {
        return NumberUtil.parseLong(userId, null);
    }

    private List<WmsCheckOrderDetailRespVO> buildCheckOrderDetailRespVOList(List<WmsCheckOrderDetailDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 获取相关的商品、SKU、仓库等数据
        Map<Long, WmsItemSkuDO> skuMap = itemSkuService.getItemSkuMap(convertSet(list, WmsCheckOrderDetailDO::getSkuId));
        Map<Long, WmsItemDO> itemMap = itemService.getItemMap(convertSet(skuMap.values(), WmsItemSkuDO::getItemId));
        Map<Long, WmsWarehouseDO> warehouseMap = warehouseService.getWarehouseMap(
                convertSet(list, WmsCheckOrderDetailDO::getWarehouseId));
        // 拼接数据
        return BeanUtils.toBean(list, WmsCheckOrderDetailRespVO.class, vo -> {
            MapUtils.findAndThen(skuMap, vo.getSkuId(), sku -> {
                vo.setSkuCode(sku.getCode()).setSkuName(sku.getName()).setItemId(sku.getItemId());
                MapUtils.findAndThen(itemMap, sku.getItemId(), item -> vo.setItemCode(item.getCode())
                        .setItemName(item.getName()).setUnit(item.getUnit()));
            });
            MapUtils.findAndThen(warehouseMap, vo.getWarehouseId(), warehouse -> vo.setWarehouseName(warehouse.getName()));
        });
    }

}
