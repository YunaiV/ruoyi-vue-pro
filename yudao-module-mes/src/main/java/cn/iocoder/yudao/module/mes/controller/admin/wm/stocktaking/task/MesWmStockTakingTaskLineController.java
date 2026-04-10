package cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.task;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.task.vo.line.MesWmStockTakingTaskLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.task.vo.line.MesWmStockTakingTaskLineRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.task.vo.line.MesWmStockTakingTaskLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.stocktaking.task.MesWmStockTakingTaskLineDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseAreaDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseLocationDO;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.md.unitmeasure.MesMdUnitMeasureService;
import cn.iocoder.yudao.module.mes.service.wm.stocktaking.task.MesWmStockTakingTaskLineService;
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
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;

@Tag(name = "管理后台 - MES 盘点任务行")
@RestController
@RequestMapping("/mes/wm/stocktaking-task-line")
@Validated
public class MesWmStockTakingTaskLineController {

    @Resource
    private MesWmStockTakingTaskLineService stockTakingTaskLineService;
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
    @Operation(summary = "获得盘点任务行")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-stock-taking-task:query')")
    public CommonResult<MesWmStockTakingTaskLineRespVO> getStockTakingTaskLine(@RequestParam("id") Long id) {
        MesWmStockTakingTaskLineDO line = stockTakingTaskLineService.getStockTakingTaskLine(id);
        if (line == null) {
            return success(null);
        }
        return success(buildStockTakingTaskLineRespVOList(Collections.singletonList(line)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得盘点任务行分页")
    @PreAuthorize("@ss.hasPermission('mes:wm-stock-taking-task:query')")
    public CommonResult<PageResult<MesWmStockTakingTaskLineRespVO>> getStockTakingTaskLinePage(
            @Valid MesWmStockTakingTaskLinePageReqVO pageReqVO) {
        PageResult<MesWmStockTakingTaskLineDO> pageResult = stockTakingTaskLineService.getStockTakingTaskLinePage(pageReqVO);
        return success(new PageResult<>(buildStockTakingTaskLineRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @PostMapping("/create")
    @Operation(summary = "创建盘点任务行")
    @PreAuthorize("@ss.hasPermission('mes:wm-stock-taking-task:update')")
    public CommonResult<Long> createStockTakingTaskLine(@Valid @RequestBody MesWmStockTakingTaskLineSaveReqVO createReqVO) {
        return success(stockTakingTaskLineService.createStockTakingTaskLine(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新盘点任务行")
    @PreAuthorize("@ss.hasPermission('mes:wm-stock-taking-task:update')")
    public CommonResult<Boolean> updateStockTakingTaskLine(@Valid @RequestBody MesWmStockTakingTaskLineSaveReqVO updateReqVO) {
        stockTakingTaskLineService.updateStockTakingTaskLine(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除盘点任务行")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-stock-taking-task:update')")
    public CommonResult<Boolean> deleteStockTakingTaskLine(@RequestParam("id") Long id) {
        stockTakingTaskLineService.deleteStockTakingTaskLine(id);
        return success(true);
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得盘点任务行精简列表", description = "根据任务编号查询，主要用于前端的下拉选项")
    @PreAuthorize("@ss.hasPermission('mes:wm-stock-taking-task:query')")
    public CommonResult<List<MesWmStockTakingTaskLineRespVO>> getStockTakingTaskLineSimpleList(
            @RequestParam("taskId") Long taskId) {
        List<MesWmStockTakingTaskLineDO> list = stockTakingTaskLineService.getStockTakingTaskLineListByTaskId(taskId);
        return success(buildStockTakingTaskLineRespVOList(list));
    }

    // ==================== 拼接 VO ====================

    private List<MesWmStockTakingTaskLineRespVO> buildStockTakingTaskLineRespVOList(List<MesWmStockTakingTaskLineDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 查询关联数据
        Map<Long, MesMdItemDO> itemMap = itemService.getItemMap(convertSet(list, MesWmStockTakingTaskLineDO::getItemId));
        Map<Long, MesMdUnitMeasureDO> unitMeasureMap = unitMeasureService.getUnitMeasureMap(
                convertSet(itemMap.values(), MesMdItemDO::getUnitMeasureId));
        Map<Long, MesWmWarehouseDO> warehouseMap = warehouseService.getWarehouseMap(
                convertSet(list, MesWmStockTakingTaskLineDO::getWarehouseId));
        Map<Long, MesWmWarehouseLocationDO> locationMap = locationService.getWarehouseLocationMap(
                convertSet(list, MesWmStockTakingTaskLineDO::getLocationId));
        Map<Long, MesWmWarehouseAreaDO> areaMap = areaService.getWarehouseAreaMap(
                convertSet(list, MesWmStockTakingTaskLineDO::getAreaId));
        // 拼接 VO
        return BeanUtils.toBean(list, MesWmStockTakingTaskLineRespVO.class, vo -> {
            findAndThen(itemMap, vo.getItemId(), item -> {
                vo.setItemCode(item.getCode()).setItemName(item.getName()).setSpecification(item.getSpecification());
                findAndThen(unitMeasureMap, item.getUnitMeasureId(),
                        unitMeasure -> vo.setUnitMeasureName(unitMeasure.getName()));
            });
            findAndThen(warehouseMap, vo.getWarehouseId(),
                    warehouse -> vo.setWarehouseName(warehouse.getName()));
            findAndThen(locationMap,
                    vo.getLocationId(), location -> vo.setLocationName(location.getName()));
            findAndThen(areaMap,
                    vo.getAreaId(), area -> vo.setAreaName(area.getName()));
        });
    }

}
