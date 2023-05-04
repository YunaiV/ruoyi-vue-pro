package cn.iocoder.yudao.module.product.service.favorite;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoritePageReqVO;
import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoriteReqVO;
import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoriteRespVO;

import javax.validation.Valid;

/**
 * 喜爱商品 Service 接口
 *
 * @author jason
 */
public interface ProductFavoriteService {

    /**
     * 商品收藏
     * @param reqVO 请求vo
     */
    Boolean collect(@Valid  AppFavoriteReqVO reqVO);

    /**
     * 取消商品收藏 (通过商品详情页面)
     * @param reqVO 请求vo
     */
    Boolean cancelCollect(@Valid AppFavoriteReqVO reqVO);

    /**
     * 分页查询用户收藏列表
     * @param reqVO 请求 vo
     */
    PageResult<AppFavoriteRespVO> pageCollectList(@Valid AppFavoritePageReqVO reqVO);
}
