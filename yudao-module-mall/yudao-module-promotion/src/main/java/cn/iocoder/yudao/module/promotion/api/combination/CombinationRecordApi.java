package cn.iocoder.yudao.module.promotion.api.combination;

import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordCreateReqDTO;
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordCreateRespDTO;
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordRespDTO;
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationValidateJoinRespDTO;
import jakarta.validation.Valid;

/**
 * 拼团记录 API 接口
 *
 * @author HUIHUI
 */
public interface CombinationRecordApi {

    /**
     * 校验是否满足拼团条件
     *
     * @param userId     用户编号
     * @param activityId 活动编号
     * @param headId     团长编号
     * @param skuId      sku 编号
     * @param count      数量
     */
    void validateCombinationRecord(Long userId, Long activityId, Long headId, Long skuId, Integer count);

    /**
     * 创建开团记录
     *
     * @param reqDTO 请求 DTO
     * @return 拼团信息
     */
    CombinationRecordCreateRespDTO createCombinationRecord(@Valid CombinationRecordCreateReqDTO reqDTO);

    /**
     * 基于订单编号，查询拼团记录
     *
     * @param userId  用户编号
     * @param orderId 订单编号
     * @return 拼团记录
     */
    CombinationRecordRespDTO getCombinationRecordByOrderId(Long userId, Long orderId);

    /**
     * 【下单前】校验是否满足拼团活动条件
     *
     * 如果校验失败，则抛出业务异常
     *
     * @param userId     用户编号
     * @param activityId 活动编号
     * @param headId     团长编号
     * @param skuId      sku 编号
     * @param count      数量
     * @return 拼团信息
     */
    CombinationValidateJoinRespDTO validateJoinCombination(Long userId, Long activityId, Long headId,
                                                           Long skuId, Integer count);

}
