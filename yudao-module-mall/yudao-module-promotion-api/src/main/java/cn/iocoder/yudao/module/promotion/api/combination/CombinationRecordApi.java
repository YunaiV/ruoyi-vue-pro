package cn.iocoder.yudao.module.promotion.api.combination;

import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordCreateReqDTO;
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordRespDTO;
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordUpdateStatusReqDTO;

import javax.validation.Valid;
import java.util.List;

// TODO @芋艿：后面也再撸撸这几个接口

/**
 * 拼团记录 API 接口
 *
 * @author HUIHUI
 */
public interface CombinationRecordApi {

    /**
     * 创建开团记录
     *
     * @param reqDTO 请求 DTO
     */
    void createCombinationRecord(@Valid CombinationRecordCreateReqDTO reqDTO);

    /**
     * 查询拼团记录是否成功
     *
     * @param userId  用户编号
     * @param orderId 订单编号
     * @return 拼团是否成功
     */
    boolean isCombinationRecordSuccess(Long userId, Long orderId);

    /**
     * 获取拼团记录
     *
     * @param userId     用户编号
     * @param activityId 活动编号
     * @return 拼团记录列表
     */
    List<CombinationRecordRespDTO> getRecordListByUserIdAndActivityId(Long userId, Long activityId);

    /**
     * 验证组合限制数
     * 校验是否满足限购要求
     *
     * @param count      本次购买数量
     * @param sumCount   已购买数量合计
     * @param activityId 活动编号
     */
    void validateCombinationLimitCount(Long activityId, Integer count, Integer sumCount);

    // TODO @puhui999：是不是搞成具体的方法，拼团成功，拼团失败，这种方法；

    /**
     * 更新开团记录状态
     *
     * @param reqDTO 请求 DTO
     */
    void updateCombinationRecordStatus(CombinationRecordUpdateStatusReqDTO reqDTO);

}
