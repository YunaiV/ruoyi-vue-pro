package cn.iocoder.yudao.module.product.controller.admin.favorite;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.module.product.controller.admin.favorite.vo.ProductFavoritePageReqVO;
import cn.iocoder.yudao.module.product.controller.admin.favorite.vo.ProductFavoriteReqVO;
import cn.iocoder.yudao.module.product.controller.admin.favorite.vo.ProductFavoriteRespVO;
import cn.iocoder.yudao.module.product.convert.favorite.ProductFavoriteConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.favorite.ProductFavoriteDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.service.favorite.ProductFavoriteService;
import cn.iocoder.yudao.module.product.service.spu.ProductSpuService;
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

@Tag(name = "管理后台 - 商品收藏")
@RestController
@RequestMapping("/product/favorite")
@Validated
public class ProductFavoriteController {

    @Resource
    private ProductFavoriteService productFavoriteService;

    @Resource
    private ProductSpuService productSpuService;

    @PostMapping("/create")
    @Operation(summary = "添加单个商品收藏")
    @PreAuthorize("@ss.hasPermission('product:favorite:create')")
    public CommonResult<Long> createFavorite(@Valid @RequestBody ProductFavoriteReqVO reqVO) {
        return success(productFavoriteService.createFavorite(reqVO.getUserId(), reqVO.getSpuId()));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "取消单个商品收藏")
    @PreAuthorize("@ss.hasPermission('product:favorite:delete')")
    public CommonResult<Boolean> deleteFavorite(@Valid @RequestBody ProductFavoriteReqVO reqVO) {
        productFavoriteService.deleteFavorite(reqVO.getUserId(), reqVO.getSpuId());
        return success(Boolean.TRUE);
    }

    @GetMapping("/page")
    @Operation(summary = "获得商品收藏分页")
    @PreAuthorize("@ss.hasPermission('product:favorite:query')")
    public CommonResult<PageResult<ProductFavoriteRespVO>> getFavoritePage(@Valid ProductFavoritePageReqVO pageVO) {
        PageResult<ProductFavoriteDO> favoritePage = productFavoriteService.getFavoritePage(pageVO);
        if (CollUtil.isEmpty(favoritePage.getList())) {
            return success(PageResult.empty());
        }

        List<ProductSpuDO> list = productSpuService.getSpuList(convertSet(favoritePage.getList(), ProductFavoriteDO::getSpuId));

        // 得到商品 spu 信息
        List<ProductFavoriteRespVO> favorites =  ProductFavoriteConvert.INSTANCE.convertList2admin(favoritePage.getList(), list);

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
}
