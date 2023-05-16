package cn.iocoder.yudao.module.product.service.favorite;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoritePageReqVO;
import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoriteReqVO;
import cn.iocoder.yudao.module.product.convert.favorite.ProductFavoriteConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.favorite.ProductFavoriteDO;
import cn.iocoder.yudao.module.product.dal.mysql.favorite.ProductFavoriteMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.FAVORITE_EXISTS;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.FAVORITE_NOT_EXISTS;

/**
 * 商品收藏 Service 实现类
 *
 * @author jason
 */
@Service
@Validated
public class ProductFavoriteServiceImpl implements ProductFavoriteService {

    @Resource
    private ProductFavoriteMapper productFavoriteMapper;

    @Override
    public Long createFavorite(Long userId, @Valid AppFavoriteReqVO reqVO) {
        ProductFavoriteDO favorite = productFavoriteMapper.selectByUserAndSpuAndType(
                userId, reqVO.getSpuId(), reqVO.getType());
        if (Objects.nonNull(favorite)) {
            throw exception(FAVORITE_EXISTS);
        }

        ProductFavoriteDO entity = ProductFavoriteConvert.INSTANCE.convert(userId, reqVO);
        productFavoriteMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void deleteFavorite(Long userId, @Valid AppFavoriteReqVO reqVO) {
        ProductFavoriteDO favorite = productFavoriteMapper.selectByUserAndSpuAndType(
                userId, reqVO.getSpuId(), reqVO.getType());
        if (Objects.isNull(favorite)) {
            throw exception(FAVORITE_NOT_EXISTS);
        }

        productFavoriteMapper.deleteById(favorite.getId());
    }

    @Override
    public PageResult<ProductFavoriteDO> getFavoritePage(Long userId, @Valid AppFavoritePageReqVO reqVO) {
        return productFavoriteMapper.selectPageByUserAndType(userId, reqVO.getType(), reqVO);
    }

    @Override
    public ProductFavoriteDO getFavorite(Long userId, @Valid AppFavoriteReqVO reqVO) {
        return productFavoriteMapper.selectByUserAndSpuAndType(userId, reqVO.getSpuId(), reqVO.getType());
    }

}
