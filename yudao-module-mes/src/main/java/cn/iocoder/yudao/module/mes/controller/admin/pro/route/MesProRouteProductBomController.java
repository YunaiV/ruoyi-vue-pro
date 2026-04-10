package cn.iocoder.yudao.module.mes.controller.admin.pro.route;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.pro.route.vo.productbom.MesProRouteProductBomRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.route.vo.productbom.MesProRouteProductBomSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.route.MesProRouteProductBomDO;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.md.unitmeasure.MesMdUnitMeasureService;
import cn.iocoder.yudao.module.mes.service.pro.route.MesProRouteProductBomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
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
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

@Tag(name = "管理后台 - MES 工艺路线产品 BOM")
@RestController
@RequestMapping("/mes/pro/route-product-bom")
@Validated
public class MesProRouteProductBomController {

    @Resource
    private MesProRouteProductBomService routeProductBomService;

    @Resource
    private MesMdItemService itemService;

    @Resource
    private MesMdUnitMeasureService unitMeasureService;

    @PostMapping("/create")
    @Operation(summary = "创建工艺路线产品 BOM")
    @PreAuthorize("@ss.hasPermission('mes:pro-route:update')")
    public CommonResult<Long> createRouteProductBom(@Valid @RequestBody MesProRouteProductBomSaveReqVO createReqVO) {
        return success(routeProductBomService.createRouteProductBom(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新工艺路线产品 BOM")
    @PreAuthorize("@ss.hasPermission('mes:pro-route:update')")
    public CommonResult<Boolean> updateRouteProductBom(@Valid @RequestBody MesProRouteProductBomSaveReqVO updateReqVO) {
        routeProductBomService.updateRouteProductBom(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除工艺路线产品 BOM")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:pro-route:update')")
    public CommonResult<Boolean> deleteRouteProductBom(@RequestParam("id") Long id) {
        routeProductBomService.deleteRouteProductBom(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得工艺路线产品 BOM")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:pro-route:query')")
    public CommonResult<MesProRouteProductBomRespVO> getRouteProductBom(@RequestParam("id") Long id) {
        MesProRouteProductBomDO routeProductBom = routeProductBomService.getRouteProductBom(id);
        return success(buildRouteProductBomRespVO(routeProductBom));
    }

    @GetMapping("/list")
    @Operation(summary = "查询工艺路线产品 BOM 列表")
    @Parameters({
            @Parameter(name = "routeId", description = "工艺路线编号", required = true, example = "1"),
            @Parameter(name = "processId", description = "工序编号", example = "1"),
            @Parameter(name = "productId", description = "产品物料编号", example = "1")
    })
    @PreAuthorize("@ss.hasPermission('mes:pro-route:query')")
    public CommonResult<List<MesProRouteProductBomRespVO>> getRouteProductBomList(
            @RequestParam("routeId") Long routeId,
            @RequestParam(value = "processId", required = false) Long processId,
            @RequestParam(value = "productId", required = false) Long productId) {
        List<MesProRouteProductBomDO> list = routeProductBomService.getRouteProductBomList(routeId, processId, productId);
        return success(buildRouteProductBomRespVOList(list));
    }

    // ==================== 拼接 VO ====================

    private List<MesProRouteProductBomRespVO> buildRouteProductBomRespVOList(List<MesProRouteProductBomDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 批量获取关联数据
        Map<Long, MesMdItemDO> itemMap = itemService.getItemMap(
                convertSet(list, MesProRouteProductBomDO::getItemId));
        Map<Long, MesMdUnitMeasureDO> unitMap = unitMeasureService.getUnitMeasureMap(
                convertSet(itemMap.values(), MesMdItemDO::getUnitMeasureId));
        // 2. 拼接 VO
        return BeanUtils.toBean(list, MesProRouteProductBomRespVO.class, vo -> {
            MapUtils.findAndThen(itemMap, vo.getItemId(), item -> {
                vo.setItemCode(item.getCode()).setItemName(item.getName())
                        .setSpecification(item.getSpecification());
                MapUtils.findAndThen(unitMap, item.getUnitMeasureId(),
                        unit -> vo.setUnitName(unit.getName()));
            });
        });
    }

    private MesProRouteProductBomRespVO buildRouteProductBomRespVO(MesProRouteProductBomDO routeProductBom) {
        if (routeProductBom == null) {
            return null;
        }
        return buildRouteProductBomRespVOList(ListUtil.of(routeProductBom)).get(0);
    }

}
