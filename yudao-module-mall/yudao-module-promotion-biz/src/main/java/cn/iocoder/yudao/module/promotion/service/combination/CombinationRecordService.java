package cn.iocoder.yudao.module.promotion.service.combination;

import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordCreateReqDTO;
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordUpdateStatusReqDTO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationRecordDO;

import java.util.List;

/**
 * 商品活动记录 service
 *
 * @author HUIHUI
 */
public interface CombinationRecordService {

    /**
     * 更新拼团状态
     *
     * @param reqDTO 请求 DTO
     */
    void updateCombinationRecordStatusByUserIdAndOrderId(CombinationRecordUpdateStatusReqDTO reqDTO);

    /**
     * 创建拼团记录
     *
     * @param reqDTO 创建信息
     */
    void createCombinationRecord(CombinationRecordCreateReqDTO reqDTO);

    /**
     * 更新拼团状态和开始时间
     *
     * @param reqDTO 请求 DTO
     */
    void updateCombinationRecordStatusAndStartTimeByUserIdAndOrderId(CombinationRecordUpdateStatusReqDTO reqDTO);

    /**
     * 获得拼团状态
     *
     * @param userId  用户编号
     * @param orderId 订单编号
     * @return 拼团状态
     */
    CombinationRecordDO getCombinationRecord(Long userId, Long orderId);

    /**
     * 获取拼团记录
     *
     * @param userId     用户 id
     * @param activityId 活动 id
     * @return 拼团记录列表
     */
    List<CombinationRecordDO> getRecordListByUserIdAndActivityId(Long userId, Long activityId);

    /**
     * 验证组合限制数
     * 校验是否满足限购要求
     *
     * @param count      本次购买数量
     * @param sumCount   已购买数量合计
     * @param activityId 活动编号
     */
    void validateCombinationLimitCount(Long activityId, Integer count, Integer sumCount);

}
