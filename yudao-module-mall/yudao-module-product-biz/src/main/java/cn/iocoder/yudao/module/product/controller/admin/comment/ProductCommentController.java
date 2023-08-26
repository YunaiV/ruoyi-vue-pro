package cn.iocoder.yudao.module.product.controller.admin.comment;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.*;
import cn.iocoder.yudao.module.product.convert.comment.ProductCommentConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.comment.ProductCommentDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.service.comment.ProductCommentService;
import cn.iocoder.yudao.module.product.service.sku.ProductSkuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 商品评价")
@RestController
@RequestMapping("/product/comment")
@Validated
public class ProductCommentController {

    @Resource
    private ProductCommentService productCommentService;
    @Resource
    private ProductSkuService productSkuService;

    @GetMapping("/page")
    @Operation(summary = "获得商品评价分页")
    @PreAuthorize("@ss.hasPermission('product:comment:query')")
    public CommonResult<PageResult<ProductCommentRespVO>> getCommentPage(@Valid ProductCommentPageReqVO pageVO) {
        PageResult<ProductCommentDO> pageResult = productCommentService.getCommentPage(pageVO);
        // 拼接返回
        List<ProductSkuDO> skuList = productSkuService.getSkuList(
                convertSet(pageResult.getList(), ProductCommentDO::getSkuId));
        return success(ProductCommentConvert.INSTANCE.convertPage(pageResult, skuList));
    }

    @PutMapping("/update-visible")
    @Operation(summary = "显示 / 隐藏评论")
    @PreAuthorize("@ss.hasPermission('product:comment:update')")
    public CommonResult<Boolean> updateCommentVisible(@Valid @RequestBody ProductCommentUpdateVisibleReqVO updateReqVO) {
        productCommentService.updateCommentVisible(updateReqVO);
        return success(true);
    }

    @PutMapping("/reply")
    @Operation(summary = "商家回复")
    @PreAuthorize("@ss.hasPermission('product:comment:update')")
    public CommonResult<Boolean> commentReply(@Valid @RequestBody ProductCommentReplyReqVO replyVO) {
        productCommentService.replyComment(replyVO, getLoginUserId());
        return success(true);
    }

    @PostMapping("/create")
    @Operation(summary = "添加自评")
    @PreAuthorize("@ss.hasPermission('product:comment:update')")
    public CommonResult<Boolean> createComment(@Valid @RequestBody ProductCommentCreateReqVO createReqVO) {
        productCommentService.createComment(createReqVO);
        return success(true);
    }

}
