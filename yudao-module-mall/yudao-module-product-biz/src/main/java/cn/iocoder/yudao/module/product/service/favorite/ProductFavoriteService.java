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
     * @param userId 用户id
     * @param reqVO 请求vo
     */
    Boolean create(Long userId, @Valid AppFavoriteReqVO reqVO);

    /**
     * 取消商品收藏 (通过商品详情页面)
     * @param userId 用户id
     * @param reqVO 请求vo
     */
    Boolean delete(Long userId, @Valid AppFavoriteReqVO reqVO);

    /**
     * 分页查询用户收藏列表
     * @param userId 用户id
     * @param reqVO 请求 vo
     */
    PageResult<AppFavoriteRespVO> page(Long userId, @Valid AppFavoritePageReqVO reqVO);

    /**
     * 检查是否收藏过商品
     * @param userId 用户id
     * @param reqVO 请求 vo
     * @return true: 已收藏  false: 未收藏
     */
    Boolean checkFavorite(Long userId, @Valid AppFavoriteReqVO reqVO);
}
