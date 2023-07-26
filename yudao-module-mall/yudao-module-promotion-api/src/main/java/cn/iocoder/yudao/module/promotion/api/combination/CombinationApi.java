package cn.iocoder.yudao.module.promotion.api.combination;

import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordReqDTO;

import javax.validation.Valid;

/**
 * 拼团活动 API 接口
 *
 * @author HUIHUI
 */
public interface CombinationApi {

    /**
     * 创建开团记录
     *
     * @param reqDTO 请求 DTO
     */
    void createRecord(@Valid CombinationRecordReqDTO reqDTO);

    /**
     * 获取开团记录状态
     *
     * @param userId  用户编号
     * @param orderId 订单编号
     */
    boolean validateRecordStatusIsSuccess(Long userId, Long orderId);


    /**
     * 更新开团记录状态
     *
     * @param userId  用户编号
     * @param orderId 订单编号
     * @param status  状态值
     */
    void updateRecordStatus(Long userId, Long orderId, Integer status);

    /**
     * 更新开团记录状态和开始时间
     *
     * @param userId  用户编号
     * @param orderId 订单编号
     * @param status  状态值
     * @return
     */
    void updateRecordStatusAndStartTime(Long userId, Long orderId, Integer status);

}
