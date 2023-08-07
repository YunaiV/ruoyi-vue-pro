package cn.iocoder.yudao.module.promotion.api.combination;

import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordCreateReqDTO;
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordUpdateReqDTO;

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
     * 查询拼团记录是否成功
     *
     * @param userId  用户编号
     * @param orderId 订单编号
     * @return 拼团是否成功
     */
    boolean isRecordSuccess(Long userId, Long orderId);

    /**
     * 更新开团记录状态
     *
     * @param reqDTO 请求 DTO
     */
    void updateRecordStatus(CombinationRecordUpdateReqDTO reqDTO);

}
