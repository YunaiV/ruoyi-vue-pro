package cn.iocoder.yudao.module.wms.controller.admin.md.item;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.category.WmsItemCategoryListReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.category.WmsItemCategoryRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.category.WmsItemCategorySaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemCategoryDO;
import cn.iocoder.yudao.module.wms.service.md.item.WmsItemCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - WMS 商品分类")
@RestController
@RequestMapping("/wms/item-category")
@Validated
public class WmsItemCategoryController {

    @Resource
    private WmsItemCategoryService categoryService;

    @PostMapping("/create")
    @Operation(summary = "创建商品分类")
    @PreAuthorize("@ss.hasPermission('wms:item-category:create')")
    public CommonResult<Long> createItemCategory(@Valid @RequestBody WmsItemCategorySaveReqVO createReqVO) {
        return success(categoryService.createItemCategory(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新商品分类")
    @PreAuthorize("@ss.hasPermission('wms:item-category:update')")
    public CommonResult<Boolean> updateItemCategory(@Valid @RequestBody WmsItemCategorySaveReqVO updateReqVO) {
        categoryService.updateItemCategory(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除商品分类")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:item-category:delete')")
    public CommonResult<Boolean> deleteItemCategory(@RequestParam("id") Long id) {
        categoryService.deleteItemCategory(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得商品分类")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:item-category:query')")
    public CommonResult<WmsItemCategoryRespVO> getItemCategory(@RequestParam("id") Long id) {
        WmsItemCategoryDO category = categoryService.getItemCategory(id);
        return success(BeanUtils.toBean(category, WmsItemCategoryRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得商品分类列表")
    @PreAuthorize("@ss.hasPermission('wms:item-category:query')")
    public CommonResult<List<WmsItemCategoryRespVO>> getItemCategoryList(@Valid WmsItemCategoryListReqVO listReqVO) {
        List<WmsItemCategoryDO> list = categoryService.getItemCategoryList(listReqVO);
        return success(BeanUtils.toBean(list, WmsItemCategoryRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得商品分类精简列表", description = "主要用于前端下拉")
    @PreAuthorize("@ss.hasPermission('wms:item-category:query')")
    public CommonResult<List<WmsItemCategoryRespVO>> getItemCategorySimpleList() {
        List<WmsItemCategoryDO> list = categoryService.getItemCategoryList(new WmsItemCategoryListReqVO());
        return success(BeanUtils.toBean(list, WmsItemCategoryRespVO.class));
    }

}
