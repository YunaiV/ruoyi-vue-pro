package cn.iocoder.yudao.module.product.controller.app.favorite;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoritePageReqVO;
import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoriteReqVO;
import cn.iocoder.yudao.module.product.service.favorite.ProductFavoriteService;
import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoriteRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.product.enums.favorite.ProductFavoriteTypeEnum.COLLECT;

/**
 * @author jason
 */
@Tag(name = "用户 APP - 喜爱商品")
@RestController
@RequestMapping("/product/favorite")
public class AppFavoriteController {

    @Resource
    private ProductFavoriteService productFavoriteService;

    @PostMapping(value = "/collect")
    @Operation(summary = "商品收藏")
    public CommonResult<Boolean> collect(@RequestBody @Valid AppFavoriteReqVO reqVO) {
        Assert.isTrue(Objects.equals(COLLECT.getType(), reqVO.getType()), "参数type 不匹配");
        return success(productFavoriteService.collect(reqVO));
    }

    @PostMapping(value = "/cancelCollect")
    @Operation(summary = "取消商品收藏(通过商品详情)")
    public CommonResult<Boolean> cancelCollect(@RequestBody @Valid AppFavoriteReqVO reqVO) {
        Assert.isTrue(Objects.equals(COLLECT.getType(), reqVO.getType()), "参数type 不匹配");
        return success(productFavoriteService.cancelCollect(reqVO));
    }

    @GetMapping(value = "/collectList")
    @Operation(summary = "商品收藏列表")
    public CommonResult<PageResult<AppFavoriteRespVO>> pageCollectList(AppFavoritePageReqVO reqVO) {
        return success(productFavoriteService.pageCollectList(reqVO));
    }
}
