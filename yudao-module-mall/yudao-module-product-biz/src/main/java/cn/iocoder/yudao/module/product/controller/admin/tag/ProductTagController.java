package cn.iocoder.yudao.module.product.controller.admin.tag;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.admin.tag.vo.ProductTagCreateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.tag.vo.ProductTagPageReqVO;
import cn.iocoder.yudao.module.product.controller.admin.tag.vo.ProductTagRespVO;
import cn.iocoder.yudao.module.product.controller.admin.tag.vo.ProductTagUpdateReqVO;
import cn.iocoder.yudao.module.product.convert.tag.ProductTagConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.tag.ProductTagDO;
import cn.iocoder.yudao.module.product.service.tag.ProductTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 商品标签")
@RestController
@RequestMapping("/product/tag")
@Validated
public class ProductTagController {

    @Resource
    private ProductTagService tagService;

    @PostMapping("/create")
    @Operation(summary = "创建商品标签")
    @PreAuthorize("@ss.hasPermission('product:tag:create')")
    public CommonResult<Long> createTag(@Valid @RequestBody ProductTagCreateReqVO createReqVO) {
        return success(tagService.createTag(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新商品标签")
    @PreAuthorize("@ss.hasPermission('product:tag:update')")
    public CommonResult<Boolean> updateTag(@Valid @RequestBody ProductTagUpdateReqVO updateReqVO) {
        tagService.updateTag(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除商品标签")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('product:tag:delete')")
    public CommonResult<Boolean> deleteTag(@RequestParam("id") Long id) {
        tagService.deleteTag(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得商品标签")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('product:tag:query')")
    public CommonResult<ProductTagRespVO> getProductTag(@RequestParam("id") Long id) {
        ProductTagDO tag = tagService.getTag(id);
        return success(ProductTagConvert.INSTANCE.convert(tag));
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "获取商品标签精简信息列表", description = "只包含被开启的商品标签，主要用于前端的下拉选项")
    public CommonResult<List<ProductTagRespVO>> getSimpleTagList() {
        // 获用户列表，只要开启状态的
        List<ProductTagDO> list = tagService.getTagList();
        // 排序后，返回给前端
        return success(ProductTagConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/list")
    @Operation(summary = "获得商品标签列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('product:tag:query')")
    public CommonResult<List<ProductTagRespVO>> getProductTagList(@RequestParam("ids") Collection<Long> ids) {
        List<ProductTagDO> list = tagService.getTagList(ids);
        return success(ProductTagConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得商品标签分页")
    @PreAuthorize("@ss.hasPermission('product:tag:query')")
    public CommonResult<PageResult<ProductTagRespVO>> getTagPage(@Valid ProductTagPageReqVO pageVO) {
        PageResult<ProductTagDO> pageResult = tagService.getTagPage(pageVO);
        return success(ProductTagConvert.INSTANCE.convertPage(pageResult));
    }

}
