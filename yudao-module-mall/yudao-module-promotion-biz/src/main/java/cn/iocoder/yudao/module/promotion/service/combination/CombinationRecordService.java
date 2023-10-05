package cn.iocoder.yudao.module.promotion.service.combination;

import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordCreateReqDTO;
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationValidateJoinRespDTO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationProductDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationRecordDO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 拼团记录 Service 接口
 *
 * @author HUIHUI
 */
public interface CombinationRecordService {

    /**
     * 更新拼团状态
     *
     * @param status  状态
     * @param userId  用户编号
     * @param orderId 订单编号
     */
    void updateCombinationRecordStatusByUserIdAndOrderId(Integer status, Long userId, Long orderId);

    /**
     * 校验是否满足拼团条件
     * 如果不满足，会抛出异常
     *
     * @param activityId 活动编号
     * @param userId     用户编号
     * @param skuId      sku 编号
     * @param count      数量
     * @return 返回拼团活动和拼团活动商品
     */
    KeyValue<CombinationActivityDO, CombinationProductDO> validateCombinationRecord(Long activityId, Long userId, Long skuId, Integer count);

    /**
     * 创建拼团记录
     *
     * @param reqDTO 创建信息
     */
    void createCombinationRecord(CombinationRecordCreateReqDTO reqDTO);

    /**
     * 更新拼团状态和开始时间
     *
     * @param status    状态
     * @param userId    用户编号
     * @param orderId   订单编号
     * @param startTime 开始时间
     */
    void updateRecordStatusAndStartTimeByUserIdAndOrderId(Integer status, Long userId, Long orderId, LocalDateTime startTime);

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
     * 【下单前】校验是否满足拼团活动条件
     *
     * 如果校验失败，则抛出业务异常
     *
     * @param activityId 活动编号
     * @param userId     用户编号
     * @param skuId      sku 编号
     * @param count      数量
     * @return 拼团信息
     */
    CombinationValidateJoinRespDTO validateJoinCombination(Long activityId, Long userId, Long skuId, Integer count);

    /**
     * 获取所有拼团记录数
     *
     * @return 记录数
     */
    Long getRecordsCount();

    /**
     * 获取最近的 count 条拼团记录
     *
     * @param count 限制数量
     * @return 拼团记录列表
     */
    List<CombinationRecordDO> getLatestRecordList(int count);

    /**
     * 获得最近 n 条拼团记录（团长发起的）
     *
     * @param activityId 拼团活动编号
     * @param status     状态
     * @param count      数量
     * @return 拼团记录列表
     */
    List<CombinationRecordDO> getRecordListWithHead(Long activityId, Integer status, Integer count);

    /**
     * 获取指定编号的拼团记录
     *
     * @param id 拼团记录编号
     * @return 拼团记录
     */
    CombinationRecordDO getRecordById(Long id);

    /**
     * 获取指定团长编号的拼团记录
     *
     * @param headId 团长编号
     * @return 拼团记录列表
     */
    List<CombinationRecordDO> getRecordListByHeadId(Long headId);

}
