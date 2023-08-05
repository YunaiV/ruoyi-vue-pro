package cn.iocoder.yudao.module.promotion.api.combination;

import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordCreateReqDTO;

import javax.validation.Valid;

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

    // TODO @puhui999：updateRecordStatus 和 updateRecordStatusAndStartTime 看看后续是不是可以统一掉；
    /**
     * 更新开团记录状态
     *
     * @param userId  用户编号
     * @param orderId 订单编号
     * @param status  状态值
     */
    void updateCombinationRecordStatus(Long userId, Long orderId, Integer status);

    /**
     * 更新开团记录状态和开始时间
     *
     * @param userId  用户编号
     * @param orderId 订单编号
     * @param status  状态值
     */
    void updateCombinationRecordStatusAndStartTime(Long userId, Long orderId, Integer status);

}
