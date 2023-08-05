package cn.iocoder.yudao.module.promotion.service.combination;

import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordCreateReqDTO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationRecordDO;

import java.time.LocalDateTime;

/**
 * 商品活动记录 service
 *
 * @author HUIHUI
 */
public interface CombinationRecordService {

    /**
     * 更新拼团状态
     *
     * @param userId  用户编号
     * @param orderId 订单编号
     * @param status  状态
     */
    void updateCombinationRecordStatusByUserIdAndOrderId(Long userId, Long orderId, Integer status);

    /**
     * 创建拼团记录
     *
     * @param reqDTO 创建信息
     */
    void createCombinationRecord(CombinationRecordCreateReqDTO reqDTO);

    /**
     * 更新拼团状态和开始时间
     *
     * @param userId    用户编号
     * @param orderId   订单编号
     * @param status    状态
     * @param startTime 开始时间
     */
    void updateCombinationRecordStatusAndStartTimeByUserIdAndOrderId(Long userId, Long orderId,
                                                                     Integer status, LocalDateTime startTime);

    /**
     * 获得拼团状态
     *
     * @param userId  用户编号
     * @param orderId 订单编号
     * @return 拼团状态
     */
    CombinationRecordDO getCombinationRecord(Long userId, Long orderId);

}
