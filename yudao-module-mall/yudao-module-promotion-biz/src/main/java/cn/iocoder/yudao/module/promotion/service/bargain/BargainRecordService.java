package cn.iocoder.yudao.module.promotion.service.bargain;


import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainRecordDO;

import java.time.LocalDateTime;

/**
 * 商品活动记录 service
 *
 * @author HUIHUI
 */
public interface BargainRecordService {

    /**
     * 更新砍价状态
     *
     * @param userId  用户编号
     * @param orderId 订单编号
     * @param status  状态
     */
    void updateBargainRecordStatusByUserIdAndOrderId(Long userId, Long orderId, Integer status);

    ///**
    // * 创建砍价记录
    // *
    // * @param reqDTO 创建信息
    // */
    //void createBargainRecord(BargainRecordCreateReqDTO reqDTO);

    /**
     * 更新砍价状态和开始时间
     *
     * @param userId    用户编号
     * @param orderId   订单编号
     * @param status    状态
     * @param startTime 开始时间
     */
    void updateBargainRecordStatusAndStartTimeByUserIdAndOrderId(Long userId, Long orderId,
                                                                 Integer status, LocalDateTime startTime);

    /**
     * 获得砍价状态
     *
     * @param userId  用户编号
     * @param orderId 订单编号
     * @return 砍价状态
     */
    BargainRecordDO getBargainRecord(Long userId, Long orderId);

}
