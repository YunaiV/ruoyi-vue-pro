package cn.iocoder.yudao.module.product.service.favorite;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoritePageReqVO;
import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoriteReqVO;
import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoriteRespVO;

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
    Boolean createFavorite(Long userId, @Valid AppFavoriteReqVO reqVO);

    /**
     * 取消商品收藏
     *
     * @param userId 用户 id
     * @param reqVO 请求 vo
     */
    Boolean deleteFavorite(Long userId, @Valid AppFavoriteReqVO reqVO);

    // TODO @Jason：VO 拼接，可以交给 Controller 哈，Service 尽量简洁
    /**
     * 分页查询用户收藏列表
     *
     * @param userId 用户 id
     * @param reqVO 请求 vo
     */
    PageResult<AppFavoriteRespVO> getFavoritePage(Long userId, @Valid AppFavoritePageReqVO reqVO);

    // TODO @Jason 这个方法最好返回 FavoriteDO 就好。Service 要通用；
    /**
     * 检查是否收藏过商品
     *
     * @param userId 用户id
     * @param reqVO 请求 vo
     * @return true: 已收藏  false: 未收藏
     */
    Boolean checkFavorite(Long userId, @Valid AppFavoriteReqVO reqVO);

}
