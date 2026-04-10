package cn.iocoder.yudao.module.promotion.api.bargain;

import cn.iocoder.yudao.module.promotion.api.bargain.dto.BargainValidateJoinRespDTO;

/**
 * 砍价记录 API 接口
 *
 * @author HUIHUI
 */
public interface BargainRecordApi {

    /**
     * 【下单前】校验是否参与砍价活动
     * <p>
     * 如果校验失败，则抛出业务异常
     *
     * @param userId          用户编号
     * @param bargainRecordId 砍价活动编号
     * @param skuId           SKU 编号
     * @return 砍价信息
     */
    BargainValidateJoinRespDTO validateJoinBargain(Long userId, Long bargainRecordId, Long skuId);

    /**
     * 更新砍价记录的订单编号
     *
     * 在砍价成功后，用户发起订单后，会记录该订单编号
     *
     * @param id     砍价记录编号
     * @param orderId 订单编号
     */
    void updateBargainRecordOrderId(Long id, Long orderId);

}
