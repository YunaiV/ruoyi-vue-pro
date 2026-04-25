package cn.iocoder.yudao.module.product.controller.app.recommend;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.product.controller.app.recommend.vo.AppProductRecommendRespVO;
import cn.iocoder.yudao.module.product.service.recommend.ProductRecommendService;
import cn.iocoder.yudao.module.product.service.recommend.bo.ProductRecommendBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 用户 App - 智能服装推荐
 * <p>
 * 对应 Python /api/v1/clothing/recommend
 *
 * @author deepay
 */
@Tag(name = "用户 App - 智能服装推荐")
@RestController
@RequestMapping("/product/recommend")
@Validated
public class AppProductRecommendController {

    @Resource
    private ProductRecommendService productRecommendService;

    @GetMapping("/list")
    @Operation(summary = "获取个性化推荐列表（最多 10 条）")
    @Parameters({
            @Parameter(name = "budget",  description = "预算金额（分），0 表示不限", example = "100000"),
            @Parameter(name = "occasion", description = "使用场景：casual / formal / sport", example = "casual")
    })
    @PermitAll
    public CommonResult<List<AppProductRecommendRespVO>> getRecommendations(
            @RequestParam(value = "budget", defaultValue = "0") int budget,
            @RequestParam(value = "occasion", required = false) String occasion) {
        Long userId = getLoginUserId();
        List<ProductRecommendBO> bos = productRecommendService.getPersonalizedRecommendations(userId, budget, occasion);
        return success(bos.stream().map(this::toVO).collect(Collectors.toList()));
    }

    @PostMapping("/track")
    @Operation(summary = "上报行为并刷新推荐（实时个性化）")
    @Parameters({
            @Parameter(name = "action", description = "行为类型：VIEW / LIKE / DISLIKE / CART_ADD", required = true),
            @Parameter(name = "spuId",  description = "商品 SPU 编号", required = true)
    })
    public CommonResult<List<AppProductRecommendRespVO>> trackAndRefresh(
            @RequestParam("action") String action,
            @RequestParam("spuId") Long spuId) {
        Long userId = getLoginUserId();
        List<ProductRecommendBO> bos = productRecommendService.trackActionAndRefresh(userId, action, spuId);
        return success(bos.stream().map(this::toVO).collect(Collectors.toList()));
    }

    private AppProductRecommendRespVO toVO(ProductRecommendBO bo) {
        AppProductRecommendRespVO vo = new AppProductRecommendRespVO();
        vo.setSpuId(bo.getSpuId());
        vo.setSpuName(bo.getSpuName());
        vo.setPicUrl(bo.getPicUrl());
        vo.setRetailPrice(bo.getRetailPrice());
        vo.setWholesalePrice(bo.getWholesalePrice());
        vo.setDiscountPercent(bo.getDiscountPercent());
        vo.setStock(bo.getStock());
        vo.setSalesCount(bo.getSalesCount());
        vo.setScore(bo.getScore());
        vo.setReason(bo.getReason());
        vo.setDimensionScore(bo.getDimensionScore());
        return vo;
    }

}
