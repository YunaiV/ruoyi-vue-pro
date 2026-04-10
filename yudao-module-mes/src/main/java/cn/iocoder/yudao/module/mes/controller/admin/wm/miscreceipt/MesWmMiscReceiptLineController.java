package cn.iocoder.yudao.module.mes.controller.admin.wm.miscreceipt;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.miscreceipt.vo.line.MesWmMiscReceiptLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.miscreceipt.vo.line.MesWmMiscReceiptLineRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.miscreceipt.vo.line.MesWmMiscReceiptLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.miscreceipt.MesWmMiscReceiptLineDO;
import cn.iocoder.yudao.module.mes.service.wm.miscreceipt.MesWmMiscReceiptLineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseLocationDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseAreaDO;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.md.unitmeasure.MesMdUnitMeasureService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseLocationService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseAreaService;

/**
 * MES 杂项入库单行 Controller
 */
@Tag(name = "管理后台 - MES 杂项入库单行")
@RestController
@RequestMapping("/mes/wm/misc-receipt-line")
@Validated
public class MesWmMiscReceiptLineController {

    @Resource
    private MesWmMiscReceiptLineService miscReceiptLineService;

    @Resource
    private MesMdItemService itemService;

    @Resource
    private MesMdUnitMeasureService unitMeasureService;

    @Resource
    private MesWmWarehouseService warehouseService;

    @Resource
    private MesWmWarehouseLocationService warehouseLocationService;

    @Resource
    private MesWmWarehouseAreaService warehouseAreaService;

    @PostMapping("/create")
    @Operation(summary = "创建杂项入库单行")
    @PreAuthorize("@ss.hasPermission('mes:wm:misc-receipt:create')")
    public CommonResult<Long> createMiscReceiptLine(@Valid @RequestBody MesWmMiscReceiptLineSaveReqVO createReqVO) {
        return success(miscReceiptLineService.createMiscReceiptLine(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改杂项入库单行")
    @PreAuthorize("@ss.hasPermission('mes:wm:misc-receipt:update')")
    public CommonResult<Boolean> updateMiscReceiptLine(@Valid @RequestBody MesWmMiscReceiptLineSaveReqVO updateReqVO) {
        miscReceiptLineService.updateMiscReceiptLine(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除杂项入库单行")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm:misc-receipt:delete')")
    public CommonResult<Boolean> deleteMiscReceiptLine(@RequestParam("id") Long id) {
        miscReceiptLineService.deleteMiscReceiptLine(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得杂项入库单行")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:wm:misc-receipt:query')")
    public CommonResult<MesWmMiscReceiptLineRespVO> getMiscReceiptLine(@RequestParam("id") Long id) {
        MesWmMiscReceiptLineDO line = miscReceiptLineService.getMiscReceiptLine(id);
        if (line == null) {
            return success(null);
        }
        return success(buildRespVOList(Collections.singletonList(line)).get(0));
    }

    @GetMapping("/list-by-receipt-id")
    @Operation(summary = "获得杂项入库单行列表")
    @Parameter(name = "receiptId", description = "入库单编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:wm:misc-receipt:query')")
    public CommonResult<List<MesWmMiscReceiptLineRespVO>> getMiscReceiptLineListByReceiptId(@RequestParam("receiptId") Long receiptId) {
        List<MesWmMiscReceiptLineDO> list = miscReceiptLineService.getMiscReceiptLineListByReceiptId(receiptId);
        return success(buildRespVOList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得杂项入库单行分页")
    @PreAuthorize("@ss.hasPermission('mes:wm:misc-receipt:query')")
    public CommonResult<PageResult<MesWmMiscReceiptLineRespVO>> getMiscReceiptLinePage(@Valid MesWmMiscReceiptLinePageReqVO pageReqVO) {
        PageResult<MesWmMiscReceiptLineDO> pageResult = miscReceiptLineService.getMiscReceiptLinePage(pageReqVO);
        return success(new PageResult<>(buildRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    // ==================== 拼接 VO ====================

    private List<MesWmMiscReceiptLineRespVO> buildRespVOList(List<MesWmMiscReceiptLineDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 获得关联数据
        Map<Long, MesMdItemDO> itemMap = itemService.getItemMap(
                convertSet(list, MesWmMiscReceiptLineDO::getItemId));
        Map<Long, MesMdUnitMeasureDO> unitMeasureMap = unitMeasureService.getUnitMeasureMap(
                convertSet(itemMap.values(), MesMdItemDO::getUnitMeasureId));
        Map<Long, MesWmWarehouseDO> warehouseMap = warehouseService.getWarehouseMap(
                convertSet(list, MesWmMiscReceiptLineDO::getWarehouseId));
        Map<Long, MesWmWarehouseLocationDO> locationMap = warehouseLocationService.getWarehouseLocationMap(
                convertSet(list, MesWmMiscReceiptLineDO::getLocationId));
        Map<Long, MesWmWarehouseAreaDO> areaMap = warehouseAreaService.getWarehouseAreaMap(
                convertSet(list, MesWmMiscReceiptLineDO::getAreaId));

        // 2. 构建结果
        return BeanUtils.toBean(list, MesWmMiscReceiptLineRespVO.class, vo -> {
            MapUtils.findAndThen(itemMap, vo.getItemId(), item -> {
                vo.setItemCode(item.getCode()).setItemName(item.getName()).setSpecification(item.getSpecification());
                MapUtils.findAndThen(unitMeasureMap, item.getUnitMeasureId(),
                        unitMeasure -> vo.setUnitMeasureName(unitMeasure.getName()));
            });
            MapUtils.findAndThen(warehouseMap, vo.getWarehouseId(), warehouse -> vo.setWarehouseName(warehouse.getName()));
            MapUtils.findAndThen(locationMap, vo.getLocationId(), location -> vo.setLocationName(location.getName()));
            MapUtils.findAndThen(areaMap, vo.getAreaId(), area -> vo.setAreaName(area.getName()));
        });
    }

}
