package cn.iocoder.yudao.module.promotion.service.combination;

import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordCreateReqDTO;
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordUpdateStatusReqDTO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationRecordDO;

/**
 * 拼团记录 Service 接口
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

}
