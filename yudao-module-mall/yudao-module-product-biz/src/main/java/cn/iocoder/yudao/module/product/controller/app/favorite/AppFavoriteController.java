package cn.iocoder.yudao.module.product.controller.app.favorite;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoritePageReqVO;
import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoriteReqVO;
import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoriteRespVO;
import cn.iocoder.yudao.module.product.convert.favorite.ProductFavoriteConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.favorite.ProductFavoriteDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.service.favorite.ProductFavoriteService;
import cn.iocoder.yudao.module.product.service.spu.ProductSpuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 商品收藏")
@RestController
@RequestMapping("/product/favorite")
public class AppFavoriteController {

    @Resource
    private ProductFavoriteService productFavoriteService;
    @Resource
    private ProductSpuService productSpuService;

    @PostMapping(value = "/create")
    @Operation(summary = "商品收藏")
    //@PreAuthenticated  TODO 暂时注释
    public CommonResult<Long> createFavorite(@RequestBody @Valid AppFavoriteReqVO reqVO) {
        return success(productFavoriteService.createFavorite(getLoginUserId(), reqVO));
    }

    @DeleteMapping(value = "/delete")
    @Operation(summary = "取消商品收藏")
    public CommonResult<Boolean> deleteFavorite(@RequestBody @Valid AppFavoriteReqVO reqVO) {
        productFavoriteService.deleteFavorite(getLoginUserId(), reqVO);
        return success(Boolean.TRUE);
    }

    @GetMapping(value = "/page")
    @Operation(summary = "分页获取商品收藏列表")
    public CommonResult<PageResult<AppFavoriteRespVO>> getFavoritePage(AppFavoritePageReqVO reqVO) {
        PageResult<ProductFavoriteDO> favorites = productFavoriteService.getFavoritePage(getLoginUserId(), reqVO);
        if (favorites.getTotal() <= 0) {
            return success(PageResult.empty());
        }
        List<ProductFavoriteDO> productFavoriteList = favorites.getList();
        // 得到商品 spu 信息
        List<Long> spuIds = CollectionUtils.convertList(productFavoriteList, ProductFavoriteDO::getSpuId);
        List<ProductSpuDO> spuList = productSpuService.getSpuList(spuIds);
        //转换 VO
        PageResult<AppFavoriteRespVO> pageResult = new PageResult<>(favorites.getTotal());
        pageResult.setList(ProductFavoriteConvert.INSTANCE.convertList(productFavoriteList, spuList));
        return success(pageResult);
    }


    @GetMapping(value = "/exits")
    @Operation(summary = "检查是否收藏过商品")
    public CommonResult<Boolean> isFavoriteExists(AppFavoriteReqVO reqVO) {
        ProductFavoriteDO favoriteDO = productFavoriteService.getFavorite(getLoginUserId(), reqVO);
        return success(Objects.nonNull(favoriteDO));
    }

}
