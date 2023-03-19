package cn.iocoder.yudao.module.product.controller.admin.comment;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentPageReqVO;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentRespVO;
import cn.iocoder.yudao.module.product.convert.comment.ProductCommentConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.comment.ProductCommentDO;
import cn.iocoder.yudao.module.product.service.comment.ProductCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 商品评价")
@RestController
@RequestMapping("/product/comment")
@Validated
public class ProductCommentController {

    @Resource
    private ProductCommentService productCommentService;

    @GetMapping("/page")
    @Operation(summary = "获得商品评价分页")
    @PreAuthorize("@ss.hasPermission('product:comment:query')")
    public CommonResult<PageResult<ProductCommentRespVO>> getCommentPage(@Valid ProductCommentPageReqVO pageVO) {
        PageResult<ProductCommentDO> pageResult = productCommentService.getCommentPage(pageVO);
        return success(ProductCommentConvert.INSTANCE.convertPage(pageResult));
    }

}
