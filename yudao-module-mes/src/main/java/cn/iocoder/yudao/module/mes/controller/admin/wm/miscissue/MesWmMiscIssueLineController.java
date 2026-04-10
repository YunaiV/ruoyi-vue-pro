package cn.iocoder.yudao.module.mes.controller.admin.wm.miscissue;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.miscissue.vo.line.MesWmMiscIssueLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.miscissue.vo.line.MesWmMiscIssueLineRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.miscissue.vo.line.MesWmMiscIssueLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.miscissue.MesWmMiscIssueLineDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseLocationDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseAreaDO;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.md.unitmeasure.MesMdUnitMeasureService;
import cn.iocoder.yudao.module.mes.service.wm.miscissue.MesWmMiscIssueLineService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseLocationService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseAreaService;
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

@Tag(name = "管理后台 - MES 杂项出库单行")
@RestController
@RequestMapping("/mes/wm/misc-issue-line")
@Validated
public class MesWmMiscIssueLineController {

    @Resource
    private MesWmMiscIssueLineService miscIssueLineService;

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
    @Operation(summary = "创建杂项出库单行")
    @PreAuthorize("@ss.hasPermission('mes:wm-misc-issue:create')")
    public CommonResult<Long> createMiscIssueLine(@Valid @RequestBody MesWmMiscIssueLineSaveReqVO createReqVO) {
        return success(miscIssueLineService.createMiscIssueLine(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改杂项出库单行")
    @PreAuthorize("@ss.hasPermission('mes:wm-misc-issue:update')")
    public CommonResult<Boolean> updateMiscIssueLine(@Valid @RequestBody MesWmMiscIssueLineSaveReqVO updateReqVO) {
        miscIssueLineService.updateMiscIssueLine(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除杂项出库单行")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-misc-issue:delete')")
    public CommonResult<Boolean> deleteMiscIssueLine(@RequestParam("id") Long id) {
        miscIssueLineService.deleteMiscIssueLine(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得杂项出库单行")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:wm-misc-issue:query')")
    public CommonResult<MesWmMiscIssueLineRespVO> getMiscIssueLine(@RequestParam("id") Long id) {
        MesWmMiscIssueLineDO line = miscIssueLineService.getMiscIssueLine(id);
        if (line == null) {
            return success(null);
        }
        return success(buildRespVOList(Collections.singletonList(line)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得杂项出库单行分页")
    @PreAuthorize("@ss.hasPermission('mes:wm-misc-issue:query')")
    public CommonResult<PageResult<MesWmMiscIssueLineRespVO>> getMiscIssueLinePage(
            @Valid MesWmMiscIssueLinePageReqVO pageReqVO) {
        PageResult<MesWmMiscIssueLineDO> pageResult = miscIssueLineService.getMiscIssueLinePage(pageReqVO);
        return success(new PageResult<>(buildRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    // ==================== 拼接 VO ====================

    private List<MesWmMiscIssueLineRespVO> buildRespVOList(List<MesWmMiscIssueLineDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 获得关联数据
        Map<Long, MesMdItemDO> itemMap = itemService.getItemMap(
                convertSet(list, MesWmMiscIssueLineDO::getItemId));
        Map<Long, MesMdUnitMeasureDO> unitMeasureMap = unitMeasureService.getUnitMeasureMap(
                convertSet(itemMap.values(), MesMdItemDO::getUnitMeasureId));
        Map<Long, MesWmWarehouseDO> warehouseMap = warehouseService.getWarehouseMap(
                convertSet(list, MesWmMiscIssueLineDO::getWarehouseId));
        Map<Long, MesWmWarehouseLocationDO> locationMap = warehouseLocationService.getWarehouseLocationMap(
                convertSet(list, MesWmMiscIssueLineDO::getLocationId));
        Map<Long, MesWmWarehouseAreaDO> areaMap = warehouseAreaService.getWarehouseAreaMap(
                convertSet(list, MesWmMiscIssueLineDO::getAreaId));

        // 2. 构建结果
        return BeanUtils.toBean(list, MesWmMiscIssueLineRespVO.class, vo -> {
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
