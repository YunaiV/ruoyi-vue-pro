package cn.iocoder.yudao.module.mes.controller.admin.wm.transfer;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.transfer.vo.detail.MesWmTransferDetailRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.transfer.vo.detail.MesWmTransferDetailSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.transfer.MesWmTransferDetailDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseAreaDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseLocationDO;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.md.unitmeasure.MesMdUnitMeasureService;
import cn.iocoder.yudao.module.mes.service.wm.transfer.MesWmTransferDetailService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseAreaService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseLocationService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseService;
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

@Tag(name = "管理后台 - MES 调拨明细")
@RestController
@RequestMapping("/mes/wm/transfer-detail")
@Validated
public class MesWmTransferDetailController {

    @Resource
    private MesWmTransferDetailService transferDetailService;

    @Resource
    private MesMdItemService itemService;

    @Resource
    private MesMdUnitMeasureService unitMeasureService;

    @Resource
    private MesWmWarehouseService warehouseService;

    @Resource
    private MesWmWarehouseLocationService locationService;

    @Resource
    private MesWmWarehouseAreaService areaService;

    @PostMapping("/create")
    @Operation(summary = "创建调拨明细")
    @PreAuthorize("@ss.hasPermission('mes:wm-transfer:create')")
    public CommonResult<Long> createTransferDetail(@Valid @RequestBody MesWmTransferDetailSaveReqVO createReqVO) {
        return success(transferDetailService.createTransferDetail(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改调拨明细")
    @PreAuthorize("@ss.hasPermission('mes:wm-transfer:update')")
    public CommonResult<Boolean> updateTransferDetail(@Valid @RequestBody MesWmTransferDetailSaveReqVO updateReqVO) {
        transferDetailService.updateTransferDetail(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除调拨明细")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-transfer:delete')")
    public CommonResult<Boolean> deleteTransferDetail(@RequestParam("id") Long id) {
        transferDetailService.deleteTransferDetail(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得调拨明细")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:wm-transfer:query')")
    public CommonResult<MesWmTransferDetailRespVO> getTransferDetail(@RequestParam("id") Long id) {
        MesWmTransferDetailDO detail = transferDetailService.getTransferDetail(id);
        if (detail == null) {
            return success(null);
        }
        return success(buildRespVOList(Collections.singletonList(detail)).get(0));
    }

    @GetMapping("/list-by-line")
    @Operation(summary = "获得调拨明细列表（按行编号）")
    @Parameter(name = "lineId", description = "行编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:wm-transfer:query')")
    public CommonResult<List<MesWmTransferDetailRespVO>> getTransferDetailListByLineId(
            @RequestParam("lineId") Long lineId) {
        List<MesWmTransferDetailDO> list = transferDetailService.getTransferDetailListByLineId(lineId);
        return success(buildRespVOList(list));
    }

    // ==================== 拼接 VO ====================

    private List<MesWmTransferDetailRespVO> buildRespVOList(List<MesWmTransferDetailDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 查询相关数据
        Map<Long, MesMdItemDO> itemMap = itemService.getItemMap(convertSet(list, MesWmTransferDetailDO::getItemId));
        Map<Long, MesMdUnitMeasureDO> unitMeasureMap = unitMeasureService.getUnitMeasureMap(
                convertSet(itemMap.values(), MesMdItemDO::getUnitMeasureId));
        Map<Long, MesWmWarehouseDO> warehouseMap = warehouseService.getWarehouseMap(
                convertSet(list, MesWmTransferDetailDO::getToWarehouseId));
        Map<Long, MesWmWarehouseLocationDO> locationMap = locationService.getWarehouseLocationMap(
                convertSet(list, MesWmTransferDetailDO::getToLocationId));
        Map<Long, MesWmWarehouseAreaDO> areaMap = areaService.getWarehouseAreaMap(
                convertSet(list, MesWmTransferDetailDO::getToAreaId));
        // 拼接数据
        return BeanUtils.toBean(list, MesWmTransferDetailRespVO.class, vo -> {
            MapUtils.findAndThen(itemMap, vo.getItemId(), item -> {
                vo.setItemCode(item.getCode()).setItemName(item.getName()).setSpecification(item.getSpecification());
                MapUtils.findAndThen(unitMeasureMap, item.getUnitMeasureId(),
                        unitMeasure -> vo.setUnitMeasureName(unitMeasure.getName()));
            });
            MapUtils.findAndThen(warehouseMap, vo.getToWarehouseId(),
                    warehouse -> vo.setToWarehouseName(warehouse.getName()));
            MapUtils.findAndThen(locationMap, vo.getToLocationId(),
                    location -> vo.setToLocationName(location.getName()));
            MapUtils.findAndThen(areaMap, vo.getToAreaId(),
                    area -> vo.setToAreaName(area.getName()));
        });
    }

}
