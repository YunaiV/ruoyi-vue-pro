package cn.iocoder.yudao.module.wms.controller.admin.md.item;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.sku.WmsItemSkuPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.sku.WmsItemSkuRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemBrandDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemCategoryDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemSkuDO;
import cn.iocoder.yudao.module.wms.service.md.item.WmsItemBrandService;
import cn.iocoder.yudao.module.wms.service.md.item.WmsItemCategoryService;
import cn.iocoder.yudao.module.wms.service.md.item.WmsItemService;
import cn.iocoder.yudao.module.wms.service.md.item.WmsItemSkuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * WMS 商品 SKU Controller。
 *
 * <p>SKU 维度的查询入口：弹窗 / 选择器场景使用，按 SKU 一行展开，支持商品 / 品牌 / 分类多表联查筛选。
 * 复用商品权限 {@code wms:item:query}，不单独建权限点（lite 也是商品/SKU 共用 {@code wms:item:list}）。
 *
 * <p>SKU 的新增 / 修改 / 删除仍随商品弹窗一并维护，不在本 Controller 暴露。
 */
@Tag(name = "管理后台 - WMS 商品 SKU")
@RestController
@RequestMapping("/wms/item-sku")
@Validated
public class WmsItemSkuController {

    @Resource
    private WmsItemSkuService itemSkuService;
    @Resource
    private WmsItemService itemService;
    @Resource
    private WmsItemCategoryService categoryService;
    @Resource
    private WmsItemBrandService brandService;

    @GetMapping("/page")
    @Operation(summary = "获得商品 SKU 分页", description = "按 SKU 维度分页，支持商品 / 品牌 / 分类多表联查筛选")
    @PreAuthorize("@ss.hasPermission('wms:item:query')")
    public CommonResult<PageResult<WmsItemSkuRespVO>> getItemSkuPage(@Valid WmsItemSkuPageReqVO pageReqVO) {
        PageResult<WmsItemSkuDO> pageResult = itemSkuService.getItemSkuPage(pageReqVO);
        return success(new PageResult<>(buildItemSkuRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    // ==================== 拼接 VO ====================

    private List<WmsItemSkuRespVO> buildItemSkuRespVOList(List<WmsItemSkuDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 查询关联数据
        Map<Long, WmsItemDO> itemMap = itemService.getItemMap(convertSet(list, WmsItemSkuDO::getItemId));
        Map<Long, WmsItemCategoryDO> categoryMap = categoryService.getItemCategoryMap(
                convertSet(itemMap.values(), WmsItemDO::getCategoryId));
        Map<Long, WmsItemBrandDO> brandMap = brandService.getItemBrandMap(
                convertSet(itemMap.values(), WmsItemDO::getBrandId));
        // 拼接 VO
        return BeanUtils.toBean(list, WmsItemSkuRespVO.class, vo -> MapUtils.findAndThen(itemMap, vo.getItemId(), item -> {
            vo.setItemCode(item.getCode()).setItemName(item.getName()).setUnit(item.getUnit())
                    .setCategoryId(item.getCategoryId()).setBrandId(item.getBrandId());
            MapUtils.findAndThen(categoryMap, item.getCategoryId(), category ->
                    vo.setCategoryName(category.getName()));
            MapUtils.findAndThen(brandMap, item.getBrandId(), brand ->
                    vo.setBrandName(brand.getName()));
        }));
    }

}
