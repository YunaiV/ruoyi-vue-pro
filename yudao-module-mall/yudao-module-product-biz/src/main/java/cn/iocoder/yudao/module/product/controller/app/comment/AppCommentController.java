package cn.iocoder.yudao.module.product.controller.app.comment;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.product.controller.app.comment.vo.AppCommentAdditionalReqVO;
import cn.iocoder.yudao.module.product.controller.app.comment.vo.AppCommentCreateReqVO;
import cn.iocoder.yudao.module.product.controller.app.comment.vo.AppCommentPageReqVO;
import cn.iocoder.yudao.module.product.controller.app.comment.vo.AppCommentRespVO;
import cn.iocoder.yudao.module.product.convert.comment.ProductCommentConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.comment.ProductCommentDO;
import cn.iocoder.yudao.module.product.service.comment.ProductCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 商品评价")
@RestController
@RequestMapping("/product/comment")
@Validated
public class AppCommentController {

    @Resource
    private ProductCommentService productCommentService;

    @Resource
    private MemberUserApi memberUserApi;

    @GetMapping("/page")
    @Operation(summary = "获得商品评价分页")
    public CommonResult<PageResult<AppCommentRespVO>> getCommentPage(@Valid AppCommentPageReqVO pageVO) {
        PageResult<ProductCommentDO> pageResult = productCommentService.getCommentPage(pageVO, Boolean.TRUE);
        return success(ProductCommentConvert.INSTANCE.convertPage02(pageResult));
    }

    @PostMapping(value = "/create")
    @Operation(summary = "创建商品评价")
    public CommonResult<Boolean> createComment(@RequestBody AppCommentCreateReqVO createReqVO) {
        // TODO: 2023/3/20 要不要判断订单、商品是否存在
        MemberUserRespDTO user = memberUserApi.getUser(getLoginUserId());
        productCommentService.createComment(ProductCommentConvert.INSTANCE.convert(user, createReqVO), Boolean.FALSE);
        return success(true);
    }

    @PostMapping(value = "/additional")
    @Operation(summary = "追加评论")
    public CommonResult<Boolean> additionalComment(@RequestBody AppCommentAdditionalReqVO createReqVO) {
        // 查询会员
        MemberUserRespDTO user = memberUserApi.getUser(getLoginUserId());
        productCommentService.additionalComment(user, createReqVO);
        return success(true);
    }

}
