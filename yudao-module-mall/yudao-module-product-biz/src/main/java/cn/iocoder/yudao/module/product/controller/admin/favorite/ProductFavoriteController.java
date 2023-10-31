package cn.iocoder.yudao.module.product.controller.admin.favorite;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.module.product.controller.admin.favorite.vo.ProductFavoriteBatchReqVO;
import cn.iocoder.yudao.module.product.controller.admin.favorite.vo.ProductFavoritePageReqVO;
import cn.iocoder.yudao.module.product.controller.admin.favorite.vo.ProductFavoriteReqVO;
import cn.iocoder.yudao.module.product.controller.admin.favorite.vo.ProductFavoriteRespVO;
import cn.iocoder.yudao.module.product.convert.favorite.ProductFavoriteConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.favorite.ProductFavoriteDO;
import cn.iocoder.yudao.module.product.dal.dataobject.favorite.ProductFavoriteDetailDO;
import cn.iocoder.yudao.module.product.service.favorite.ProductFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 商品收藏")
@RestController
@RequestMapping("/product/favorite")
@Validated
public class ProductFavoriteController {

    @Resource
    private ProductFavoriteService productFavoriteService;

    @PostMapping("/create")
    @Operation(summary = "添加单个商品收藏")
    @PreAuthorize("@ss.hasPermission('product:favorite:create')")
    public CommonResult<Long> createFavorite(@Valid @RequestBody ProductFavoriteReqVO reqVO) {
        return success(productFavoriteService.createFavorite(reqVO.getUserId(), reqVO.getSpuId()));
    }

    @PostMapping("/create-list")
    @Operation(summary = "添加多个商品收藏")
    @PreAuthorize("@ss.hasPermission('product:favorite:create')")
    public CommonResult<Boolean> createFavoriteList(@Valid @RequestBody ProductFavoriteBatchReqVO reqVO) {
        // todo @jason：待实现；如果有已经收藏的，不用报错，忽略即可；
        return success(Boolean.TRUE);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "取消单个商品收藏")
    @PreAuthorize("@ss.hasPermission('product:favorite:delete')")
    public CommonResult<Boolean> deleteFavorite(@Valid @RequestBody ProductFavoriteReqVO reqVO) {
        productFavoriteService.deleteFavorite(reqVO.getUserId(), reqVO.getSpuId());
        return success(Boolean.TRUE);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "取消单个商品收藏")
    @PreAuthorize("@ss.hasPermission('product:favorite:delete')")
    public CommonResult<Boolean> deleteFavoriteList(@Valid @RequestBody ProductFavoriteBatchReqVO reqVO) {
        // todo @jason：待实现
//        productFavoriteService.deleteFavorite(getLoginUserId(), reqVO.getSpuId());
        return success(Boolean.TRUE);
    }

    @GetMapping("/page")
    @Operation(summary = "获得商品收藏分页")
    @PreAuthorize("@ss.hasPermission('product:favorite:query')")
    public CommonResult<PageResult<ProductFavoriteRespVO>> getFavoritePage(@Valid ProductFavoritePageReqVO pageVO) {
        PageResult<ProductFavoriteDetailDO> favoritePage = productFavoriteService.getFavoritePageByFilter(pageVO);
        if (CollUtil.isEmpty(favoritePage.getList())) {
            return success(PageResult.empty());
        }

        // 得到商品 spu 信息
        List<ProductFavoriteRespVO> favorites =  ProductFavoriteConvert.INSTANCE.convertList2admin(favoritePage.getList());

        // 转换 VO 结果
        PageResult<ProductFavoriteRespVO> pageResult = new PageResult<>(favoritePage.getTotal());
        pageResult.setList(favorites);

        return success(pageResult);
    }

    @PostMapping(value = "/exits")
    @Operation(summary = "检查是否收藏过商品")
    @PreAuthenticated
    public CommonResult<Boolean> isFavoriteExists(@Valid @RequestBody ProductFavoriteReqVO reqVO) {
        ProductFavoriteDO favorite = productFavoriteService.getFavorite(reqVO.getUserId(), reqVO.getSpuId());
        return success(favorite != null);
    }

    @GetMapping(value = "/get-count")
    @Operation(summary = "获得商品收藏数量")
    @Parameter(name = "userId", description = "用户编号", required = true)
    @PreAuthenticated
    public CommonResult<Long> getFavoriteCount(@RequestParam("userId") Long userId) {
        return success(productFavoriteService.getFavoriteCount(userId));
    }

}
