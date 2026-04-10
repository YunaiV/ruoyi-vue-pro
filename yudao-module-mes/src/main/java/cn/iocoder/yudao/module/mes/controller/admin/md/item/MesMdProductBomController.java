package cn.iocoder.yudao.module.mes.controller.admin.md.item;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.bom.MesMdProductBomPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.bom.MesMdProductBomRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.bom.MesMdProductBomSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemTypeDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdProductBomDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemTypeService;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdProductBomService;
import cn.iocoder.yudao.module.mes.service.md.unitmeasure.MesMdUnitMeasureService;
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

@Tag(name = "管理后台 - MES 产品BOM")
@RestController
@RequestMapping("/mes/md/product-bom")
@Validated
public class MesMdProductBomController {

    @Resource
    private MesMdProductBomService productBomService;

    @Resource
    private MesMdItemService itemService;

    @Resource
    private MesMdItemTypeService itemTypeService;

    @Resource
    private MesMdUnitMeasureService unitMeasureService;

    @PostMapping("/create")
    @Operation(summary = "创建产品BOM")
    @PreAuthorize("@ss.hasPermission('mes:md-item:create')")
    public CommonResult<Long> createProductBom(@Valid @RequestBody MesMdProductBomSaveReqVO createReqVO) {
        return success(productBomService.createProductBom(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新产品BOM")
    @PreAuthorize("@ss.hasPermission('mes:md-item:update')")
    public CommonResult<Boolean> updateProductBom(@Valid @RequestBody MesMdProductBomSaveReqVO updateReqVO) {
        productBomService.updateProductBom(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除产品BOM")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:md-item:delete')")
    public CommonResult<Boolean> deleteProductBom(@RequestParam("id") Long id) {
        productBomService.deleteProductBom(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得产品BOM")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:md-item:query')")
    public CommonResult<MesMdProductBomRespVO> getProductBom(@RequestParam("id") Long id) {
        MesMdProductBomDO productBom = productBomService.getProductBom(id);
        return success(BeanUtils.toBean(productBom, MesMdProductBomRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得产品BOM分页")
    @PreAuthorize("@ss.hasPermission('mes:md-item:query')")
    public CommonResult<PageResult<MesMdProductBomRespVO>> getProductBomPage(@Valid MesMdProductBomPageReqVO pageReqVO) {
        PageResult<MesMdProductBomDO> pageResult = productBomService.getProductBomPage(pageReqVO);
        List<MesMdProductBomRespVO> voList = buildProductBomRespVOList(pageResult.getList());
        return success(new PageResult<>(voList, pageResult.getTotal()));
    }

    @GetMapping("/list-by-item-id")
    @Operation(summary = "根据物料产品编号获得产品BOM列表")
    @Parameter(name = "itemId", description = "物料产品编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:md-item:query')")
    public CommonResult<List<MesMdProductBomRespVO>> getProductBomListByItemId(
            @RequestParam("itemId") Long itemId) {
        List<MesMdProductBomDO> list = productBomService.getProductBomListByItemId(itemId);
        return success(buildProductBomRespVOList(list));
    }

    // ==================== 拼接 VO ====================

    private List<MesMdProductBomRespVO> buildProductBomRespVOList(List<MesMdProductBomDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1.1 批量获取 BOM 物料信息
        Map<Long, MesMdItemDO> itemMap = itemService.getItemMap(
                convertSet(list, MesMdProductBomDO::getBomItemId));
        // 1.2 批量获取计量单位信息
        Map<Long, MesMdUnitMeasureDO> unitMeasureMap = unitMeasureService.getUnitMeasureMap(
                convertSet(itemMap.values(), MesMdItemDO::getUnitMeasureId));
        // 1.3 批量获取物料分类信息（用于 itemOrProduct）
        Map<Long, MesMdItemTypeDO> itemTypeMap = itemTypeService.getItemTypeMap(
                convertSet(itemMap.values(), MesMdItemDO::getItemTypeId));
        // 2. 拼接 VO
        return BeanUtils.toBean(list, MesMdProductBomRespVO.class, vo -> {
            MapUtils.findAndThen(itemMap, vo.getBomItemId(), item -> {
                vo.setBomItemCode(item.getCode());
                vo.setBomItemName(item.getName());
                vo.setBomItemSpecification(item.getSpecification());
                MapUtils.findAndThen(unitMeasureMap, item.getUnitMeasureId(),
                        unitMeasure -> vo.setUnitMeasureName(unitMeasure.getName()));
                MapUtils.findAndThen(itemTypeMap, item.getItemTypeId(),
                        itemType -> vo.setItemOrProduct(itemType.getItemOrProduct()));
            });
        });
    }

}
