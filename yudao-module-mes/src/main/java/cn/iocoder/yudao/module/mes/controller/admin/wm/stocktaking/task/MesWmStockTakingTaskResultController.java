package cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.task;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.task.vo.result.MesWmStockTakingTaskResultPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.task.vo.result.MesWmStockTakingTaskResultRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.task.vo.result.MesWmStockTakingTaskResultSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.stocktaking.task.MesWmStockTakingTaskResultDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseAreaDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseLocationDO;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.md.unitmeasure.MesMdUnitMeasureService;
import cn.iocoder.yudao.module.mes.service.wm.stocktaking.task.MesWmStockTakingTaskResultService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseAreaService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseLocationService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - MES 盘点结果")
@RestController
@RequestMapping("/mes/wm/stocktaking-task-result")
@Validated
public class MesWmStockTakingTaskResultController {

    @Resource
    private MesWmStockTakingTaskResultService stockTakingTaskResultService;
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

    @GetMapping("/get")
    @Operation(summary = "获得盘点结果")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-stock-taking-task:query')")
    public CommonResult<MesWmStockTakingTaskResultRespVO> getStockTakingTaskResult(@RequestParam("id") Long id) {
        MesWmStockTakingTaskResultDO result = stockTakingTaskResultService.getStockTakingTaskResult(id);
        if (result == null) {
            return success(null);
        }
        return success(buildStockTakingTaskResultRespVOList(Collections.singletonList(result)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得盘点结果分页")
    @PreAuthorize("@ss.hasPermission('mes:wm-stock-taking-task:query')")
    public CommonResult<PageResult<MesWmStockTakingTaskResultRespVO>> getStockTakingTaskResultPage(
            @Valid MesWmStockTakingTaskResultPageReqVO pageReqVO) {
        PageResult<MesWmStockTakingTaskResultDO> pageResult = stockTakingTaskResultService.getStockTakingTaskResultPage(pageReqVO);
        return success(new PageResult<>(buildStockTakingTaskResultRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @PostMapping("/create")
    @Operation(summary = "创建盘点结果")
    @PreAuthorize("@ss.hasPermission('mes:wm-stock-taking-task:update')")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Long> createStockTakingTaskResult(@Valid @RequestBody MesWmStockTakingTaskResultSaveReqVO createReqVO) {
        return success(stockTakingTaskResultService.createStockTakingTaskResult(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新盘点结果")
    @PreAuthorize("@ss.hasPermission('mes:wm-stock-taking-task:update')")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Boolean> updateStockTakingTaskResult(@Valid @RequestBody MesWmStockTakingTaskResultSaveReqVO updateReqVO) {
        stockTakingTaskResultService.updateStockTakingTaskResult(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除盘点结果")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-stock-taking-task:update')")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Boolean> deleteStockTakingTaskResult(@RequestParam("id") Long id) {
        stockTakingTaskResultService.deleteStockTakingTaskResult(id);
        return success(true);
    }

    // ==================== 拼接 VO ====================


    private List<MesWmStockTakingTaskResultRespVO> buildStockTakingTaskResultRespVOList(List<MesWmStockTakingTaskResultDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 查询关联数据
        Map<Long, MesMdItemDO> itemMap = itemService.getItemMap(
                convertSet(list, MesWmStockTakingTaskResultDO::getItemId));
        Map<Long, MesMdUnitMeasureDO> unitMeasureMap = unitMeasureService.getUnitMeasureMap(
                convertSet(itemMap.values(), MesMdItemDO::getUnitMeasureId));
        Map<Long, MesWmWarehouseDO> warehouseMap = warehouseService.getWarehouseMap(
                convertSet(list, MesWmStockTakingTaskResultDO::getWarehouseId));
        Map<Long, MesWmWarehouseLocationDO> locationMap = locationService.getWarehouseLocationMap(
                convertSet(list, MesWmStockTakingTaskResultDO::getLocationId));
        Map<Long, MesWmWarehouseAreaDO> areaMap = areaService.getWarehouseAreaMap(
                convertSet(list, MesWmStockTakingTaskResultDO::getAreaId));
        // 拼接 VO
        return BeanUtils.toBean(list, MesWmStockTakingTaskResultRespVO.class, vo -> {
            MapUtils.findAndThen(itemMap, vo.getItemId(), item -> {
                vo.setItemCode(item.getCode()).setItemName(item.getName()).setSpecification(item.getSpecification());
                MapUtils.findAndThen(unitMeasureMap, item.getUnitMeasureId(),
                        unit -> vo.setUnitMeasureName(unit.getName()));
            });
            MapUtils.findAndThen(warehouseMap, vo.getWarehouseId(),
                    warehouse -> vo.setWarehouseName(warehouse.getName()));
            MapUtils.findAndThen(locationMap, vo.getLocationId(),
                    location -> vo.setLocationName(location.getName()));
            MapUtils.findAndThen(areaMap, vo.getAreaId(),
                    area -> vo.setAreaName(area.getName()));
        });
    }

}
