package cn.iocoder.yudao.module.product.controller.app.favorite;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoritePageReqVO;
import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoriteReqVO;
import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoriteRespVO;
import cn.iocoder.yudao.module.product.service.favorite.ProductFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 商品收藏")
@RestController
@RequestMapping("/product/favorite")
public class AppFavoriteController {

    @Resource
    private ProductFavoriteService productFavoriteService;

    @PostMapping(value = "/create")
    @Operation(summary = "商品收藏")
    public CommonResult<Boolean> createFavorite(@RequestBody @Valid AppFavoriteReqVO reqVO) {
        return success(productFavoriteService.createFavorite(getLoginUserId(), reqVO));
    }

    @DeleteMapping(value = "/delete")
    @Operation(summary = "取消商品收藏")
    public CommonResult<Boolean> deleteFavorite(@RequestBody @Valid AppFavoriteReqVO reqVO) {
        return success(productFavoriteService.deleteFavorite(getLoginUserId(), reqVO));
    }

    @GetMapping(value = "/page")
    @Operation(summary = "分页获取商品收藏列表")
    public CommonResult<PageResult<AppFavoriteRespVO>> getFavoritePage(AppFavoritePageReqVO reqVO) {
        return success(productFavoriteService.getFavoritePage(getLoginUserId(),reqVO));
    }


    @GetMapping(value = "/exits")
    @Operation(summary = "检查是否收藏过商品")
    public CommonResult<Boolean> isFavoriteExists(AppFavoriteReqVO reqVO) {
        return success(productFavoriteService.checkFavorite(getLoginUserId(), reqVO));
    }

}
