package cn.iocoder.yudao.module.mes.controller.admin.wm.productsales;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.productsales.vo.line.MesWmProductSalesLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.productsales.vo.line.MesWmProductSalesLineRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.productsales.vo.line.MesWmProductSalesLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;

import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productsales.MesWmProductSalesLineDO;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.md.unitmeasure.MesMdUnitMeasureService;

import cn.iocoder.yudao.module.mes.service.wm.productsales.MesWmProductSalesLineService;
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

@Tag(name = "管理后台 - MES 销售出库单行")
@RestController
@RequestMapping("/mes/wm/product-sales-line")
@Validated
public class MesWmProductSalesLineController {

    @Resource
    private MesWmProductSalesLineService productSalesLineService;

    @Resource
    private MesMdItemService itemService;

    @Resource
    private MesMdUnitMeasureService unitMeasureService;


    @PostMapping("/create")
    @Operation(summary = "创建销售出库单行")
    @PreAuthorize("@ss.hasPermission('mes:wm-product-sales:create')")
    public CommonResult<Long> createProductSalesLine(@Valid @RequestBody MesWmProductSalesLineSaveReqVO createReqVO) {
        return success(productSalesLineService.createProductSalesLine(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改销售出库单行")
    @PreAuthorize("@ss.hasPermission('mes:wm-product-sales:update')")
    public CommonResult<Boolean> updateProductSalesLine(@Valid @RequestBody MesWmProductSalesLineSaveReqVO updateReqVO) {
        productSalesLineService.updateProductSalesLine(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除销售出库单行")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-product-sales:delete')")
    public CommonResult<Boolean> deleteProductSalesLine(@RequestParam("id") Long id) {
        productSalesLineService.deleteProductSalesLine(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得销售出库单行")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:wm-product-sales:query')")
    public CommonResult<MesWmProductSalesLineRespVO> getProductSalesLine(@RequestParam("id") Long id) {
        MesWmProductSalesLineDO line = productSalesLineService.getProductSalesLine(id);
        if (line == null) {
            return success(null);
        }
        return success(buildRespVOList(Collections.singletonList(line)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得销售出库单行分页")
    @PreAuthorize("@ss.hasPermission('mes:wm-product-sales:query')")
    public CommonResult<PageResult<MesWmProductSalesLineRespVO>> getProductSalesLinePage(
            @Valid MesWmProductSalesLinePageReqVO pageReqVO) {
        PageResult<MesWmProductSalesLineDO> pageResult = productSalesLineService.getProductSalesLinePage(pageReqVO);
        return success(new PageResult<>(buildRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    // ==================== 拼接 VO ====================

    private List<MesWmProductSalesLineRespVO> buildRespVOList(List<MesWmProductSalesLineDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 获得关联数据
        Map<Long, MesMdItemDO> itemMap = itemService.getItemMap(
                convertSet(list, MesWmProductSalesLineDO::getItemId));
        Map<Long, MesMdUnitMeasureDO> unitMeasureMap = unitMeasureService.getUnitMeasureMap(
                convertSet(itemMap.values(), MesMdItemDO::getUnitMeasureId));
        // 2. 构建结果
        return BeanUtils.toBean(list, MesWmProductSalesLineRespVO.class, vo -> {
            MapUtils.findAndThen(itemMap, vo.getItemId(), item -> {
                vo.setItemCode(item.getCode()).setItemName(item.getName()).setSpecification(item.getSpecification());
                MapUtils.findAndThen(unitMeasureMap, item.getUnitMeasureId(),
                        unitMeasure -> vo.setUnitMeasureName(unitMeasure.getName()));
            });

        });
    }

}
