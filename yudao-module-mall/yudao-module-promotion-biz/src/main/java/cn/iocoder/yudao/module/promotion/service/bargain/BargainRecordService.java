package cn.iocoder.yudao.module.promotion.service.bargain;


import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.api.bargain.dto.BargainValidateJoinRespDTO;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.recrod.BargainRecordPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.record.AppBargainRecordCreateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainRecordDO;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 砍价记录 service 接口
 *
 * @author HUIHUI
 */
public interface BargainRecordService {

    /**
     * 【会员】创建砍价记录（参与参加活动）
     *
     * @param userId 用户编号
     * @param reqVO 创建信息
     * @return 砍价记录编号
     */
    Long createBargainRecord(Long userId, AppBargainRecordCreateReqVO reqVO);

    /**
     * 更新砍价记录的砍价金额
     *
     * 如果满足砍价成功的条件，则更新砍价记录的状态为成功
     *
     * @param id 砍价记录编号
     * @param whereBargainPrice 当前的砍价金额
     * @param reducePrice 减少的砍价金额
     * @param success 是否砍价成功
     * @return 是否更新成功。注意，如果并发更新时，会更新失败
     */
    Boolean updateBargainRecordBargainPrice(Long id, Integer whereBargainPrice,
                                            Integer reducePrice, Boolean success);

    /**
     * 【下单前】校验是否参与砍价活动
     * <p>
     * 如果校验失败，则抛出业务异常
     *
     * @param userId          用户编号
     * @param bargainRecordId 砍价活动编号
     * @param skuId           SKU 编号
     * @return 砍价信息
     */
    BargainValidateJoinRespDTO validateJoinBargain(Long userId, Long bargainRecordId, Long skuId);

    /**
     * 更新砍价记录的订单编号
     *
     * 在砍价成功后，用户发起订单后，会记录该订单编号
     *
     * @param id     砍价记录编号
     * @param orderId 订单编号
     */
    void updateBargainRecordOrderId(Long id, Long orderId);

    /**
     * 获得砍价记录
     *
     * @param id 砍价记录编号
     * @return 砍价记录
     */
    BargainRecordDO getBargainRecord(Long id);

    /**
     * 获得用户在当前砍价活动中的最后一条砍价记录
     *
     * @param userId 用户编号
     * @param activityId 砍价记录编号
     * @return 砍价记录
     */
    BargainRecordDO getLastBargainRecord(Long userId, Long activityId);

    /**
     * 获得砍价人数 Map
     *
     * @param activityIds 活动编号
     * @param status 砍价记录状态
     * @return 砍价人数 Map
     */
    Map<Long, Integer> getBargainRecordUserCountMap(Collection<Long> activityIds, @Nullable Integer status);

    /**
     * 获得砍价人数
     *
     * @param status 砍价记录状态
     * @return 砍价人数
     */
    Integer getBargainRecordUserCount(Integer status);

    /**
     * 获得砍价人数
     *
     * @param activityId 砍价活动编号
     * @param status 砍价记录状态
     * @return 砍价人数
     */
    Integer getBargainRecordUserCount(Long activityId, Integer status);

    /**
     * 【管理员】获得砍价记录分页
     *
     * @param pageReqVO 分页查询
     * @return 砍价记录分页
     */
    PageResult<BargainRecordDO> getBargainRecordPage(BargainRecordPageReqVO pageReqVO);

    /**
     * 【会员】获得砍价记录分页
     *
     * @param userId 用户编号
     * @param pageParam 分页查询
     * @return 砍价记录分页
     */
    PageResult<BargainRecordDO> getBargainRecordPage(Long userId, PageParam pageParam);

    /**
     * 获得砍价记录列表
     *
     * @param status 砍价记录状态
     * @param count 条数
     * @return 砍价记录列表
     */
    List<BargainRecordDO> getBargainRecordList(Integer status, Integer count);

}
