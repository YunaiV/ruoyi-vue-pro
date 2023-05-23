package cn.iocoder.yudao.module.product.service.favorite;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoritePageReqVO;
import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoriteReqVO;
import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoriteRespVO;
import cn.iocoder.yudao.module.product.dal.dataobject.favorite.ProductFavoriteDO;

import javax.validation.Valid;

/**
 * 商品收藏 Service 接口
 *
 * @author jason
 */
public interface ProductFavoriteService {

    /**
     * 创建商品收藏
     *
     * @param userId 用户 id
     * @param reqVO 请求 vo
     */
    Long createFavorite(Long userId, @Valid AppFavoriteReqVO reqVO);

    /**
     * 取消商品收藏
     *
     * @param userId 用户 id
     * @param reqVO 请求 vo
     */
    void deleteFavorite(Long userId, @Valid AppFavoriteReqVO reqVO);

    /**
     * 分页查询用户收藏列表
     *
     * @param userId 用户 id
     * @param reqVO 请求 vo
     */
    PageResult<ProductFavoriteDO> getFavoritePage(Long userId, @Valid AppFavoritePageReqVO reqVO);

    /**
     * 获取收藏过商品
     *
     * @param userId 用户id
     * @param reqVO 请求 vo
     */
    ProductFavoriteDO getFavorite(Long userId, @Valid AppFavoriteReqVO reqVO);

}
