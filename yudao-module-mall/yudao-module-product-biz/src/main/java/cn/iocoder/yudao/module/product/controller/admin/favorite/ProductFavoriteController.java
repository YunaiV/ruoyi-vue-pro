package cn.iocoder.yudao.module.product.controller.admin.favorite;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.admin.favorite.vo.ProductFavoritePageReqVO;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
