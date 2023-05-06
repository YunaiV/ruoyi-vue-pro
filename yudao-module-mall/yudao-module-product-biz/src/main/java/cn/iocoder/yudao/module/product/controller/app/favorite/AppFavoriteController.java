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
    public CommonResult<Boolean> create(@RequestBody @Valid AppFavoriteReqVO reqVO) {
        Long loginUserId = getLoginUserId();
        return success(productFavoriteService.create(loginUserId, reqVO));
    }

    @DeleteMapping(value = "/delete")
    @Operation(summary = "取消商品收藏(通过商品详情)")
    public CommonResult<Boolean> delete(@RequestBody @Valid AppFavoriteReqVO reqVO) {
        Long loginUserId = getLoginUserId();
        return success(productFavoriteService.delete(loginUserId,reqVO));
    }

    @GetMapping(value = "/page")
    @Operation(summary = "分页获取商品收藏列表")
    public CommonResult<PageResult<AppFavoriteRespVO>> page(AppFavoritePageReqVO reqVO) {
        Long userId = getLoginUserId();
        return success(productFavoriteService.page(userId,reqVO));
    }

    @GetMapping(value = "/checkFavorite")
    @Operation(summary = "检查是否收藏过商品")
    public CommonResult<Boolean> checkFavorite(AppFavoriteReqVO reqVO) {
        Long userId = getLoginUserId();
        return success(productFavoriteService.checkFavorite(userId,reqVO));
    }
}
