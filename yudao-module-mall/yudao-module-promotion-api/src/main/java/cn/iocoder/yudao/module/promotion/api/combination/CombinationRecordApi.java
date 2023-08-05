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
    void createRecord(@Valid CombinationRecordCreateReqDTO reqDTO);

    /**
     * 校验拼团是否成功
     *
     * @param userId  用户编号
     * @param orderId 订单编号
     * @return 拼团是否成功
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
     */
    void updateRecordStatusAndStartTime(Long userId, Long orderId, Integer status);

}
