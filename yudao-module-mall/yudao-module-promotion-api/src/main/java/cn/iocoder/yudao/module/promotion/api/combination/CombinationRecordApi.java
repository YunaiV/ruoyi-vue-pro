package cn.iocoder.yudao.module.promotion.api.combination;

import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordCreateReqDTO;

import javax.validation.Valid;
import java.time.LocalDateTime;

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
     * 更新拼团状态为 成功
     *
     * @param userId  用户编号
     * @param orderId 订单编号
     */
    void updateRecordStatusToSuccess(Long userId, Long orderId);

    /**
     * 更新拼团状态为 失败
     *
     * @param userId  用户编号
     * @param orderId 订单编号
     */
    void updateRecordStatusToFailed(Long userId, Long orderId);

    /**
     * 更新拼团状态为 进行中
     *
     * @param userId    用户编号
     * @param orderId   订单编号
     * @param startTime 开始时间
     */
    void updateRecordStatusToInProgress(Long userId, Long orderId, LocalDateTime startTime);

}
