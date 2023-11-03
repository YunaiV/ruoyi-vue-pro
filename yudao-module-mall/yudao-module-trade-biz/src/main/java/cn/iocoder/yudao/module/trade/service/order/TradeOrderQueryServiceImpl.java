package cn.iocoder.yudao.module.trade.service.order;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderPageReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderSummaryRespVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderPageReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderItemMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.dal.redis.RedisKeyConstants;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderRefundStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.ExpressClientFactory;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressTrackQueryReqDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressTrackRespDTO;
import cn.iocoder.yudao.module.trade.service.delivery.DeliveryExpressService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.EXPRESS_NOT_EXISTS;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_NOT_FOUND;

/**
 * 交易订单【读】 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class TradeOrderQueryServiceImpl implements TradeOrderQueryService {

    @Resource
    private ExpressClientFactory expressClientFactory;

    @Resource
    private TradeOrderMapper tradeOrderMapper;
    @Resource
    private TradeOrderItemMapper tradeOrderItemMapper;

    @Resource
    private DeliveryExpressService deliveryExpressService;

    @Resource
    private MemberUserApi memberUserApi;

    // =================== Order ===================

    @Override
    public TradeOrderDO getOrder(Long id) {
        return tradeOrderMapper.selectById(id);
    }

    @Override
    public TradeOrderDO getOrder(Long userId, Long id) {
        TradeOrderDO order = tradeOrderMapper.selectById(id);
        if (order != null
                && ObjectUtil.notEqual(order.getUserId(), userId)) {
            return null;
        }
        return order;
    }

    @Override
    public TradeOrderDO getOrderByUserIdAndStatusAndCombination(Long userId, Long combinationActivityId, Integer status) {
        return tradeOrderMapper.selectByUserIdAndCombinationActivityIdAndStatus(userId, combinationActivityId, status);
    }

    @Override
    public List<TradeOrderDO> getOrderList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return tradeOrderMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<TradeOrderDO> getOrderPage(TradeOrderPageReqVO reqVO) {
        // 根据用户查询条件构建用户编号列表
        Set<Long> userIds = buildQueryConditionUserIds(reqVO);
        if (userIds == null) { // 没查询到用户，说明肯定也没他的订单
            return PageResult.empty();
        }
        // 分页查询
        return tradeOrderMapper.selectPage(reqVO, userIds);
    }

    private Set<Long> buildQueryConditionUserIds(TradeOrderPageReqVO reqVO) {
        // 获得 userId 相关的查询
        Set<Long> userIds = new HashSet<>();
        if (StrUtil.isNotEmpty(reqVO.getUserMobile())) {
            MemberUserRespDTO user = memberUserApi.getUserByMobile(reqVO.getUserMobile());
            if (user == null) { // 没查询到用户，说明肯定也没他的订单
                return null;
            }
            userIds.add(user.getId());
        }
        if (StrUtil.isNotEmpty(reqVO.getUserNickname())) {
            List<MemberUserRespDTO> users = memberUserApi.getUserListByNickname(reqVO.getUserNickname());
            if (CollUtil.isEmpty(users)) { // 没查询到用户，说明肯定也没他的订单
                return null;
            }
            userIds.addAll(convertSet(users, MemberUserRespDTO::getId));
        }
        return userIds;
    }

    @Override
    public TradeOrderSummaryRespVO getOrderSummary(TradeOrderPageReqVO reqVO) {
        // 根据用户查询条件构建用户编号列表
        Set<Long> userIds = buildQueryConditionUserIds(reqVO);
        if (userIds == null) { // 没查询到用户，说明肯定也没他的订单
            return new TradeOrderSummaryRespVO();
        }
        // 查询每个售后状态对应的数量、金额
        List<Map<String, Object>> list = tradeOrderMapper.selectOrderSummaryGroupByRefundStatus(reqVO, null);

        TradeOrderSummaryRespVO vo = new TradeOrderSummaryRespVO().setAfterSaleCount(0L).setAfterSalePrice(0L);
        for (Map<String, Object> map : list) {
            Long count = MapUtil.getLong(map, "count", 0L);
            Long price = MapUtil.getLong(map, "price", 0L);
            // 未退款的计入订单，部分退款、全部退款计入售后
            if (TradeOrderRefundStatusEnum.NONE.getStatus().equals(MapUtil.getInt(map, "refundStatus"))) {
                vo.setOrderCount(count).setOrderPayPrice(price);
            } else {
                vo.setAfterSaleCount(vo.getAfterSaleCount() + count).setAfterSalePrice(vo.getAfterSalePrice() + price);
            }
        }
        return vo;
    }

    @Override
    public PageResult<TradeOrderDO> getOrderPage(Long userId, AppTradeOrderPageReqVO reqVO) {
        return tradeOrderMapper.selectPage(reqVO, userId);
    }

    @Override
    public Long getOrderCount(Long userId, Integer status, Boolean commentStatus) {
        return tradeOrderMapper.selectCountByUserIdAndStatus(userId, status, commentStatus);
    }

    @Override
    public List<ExpressTrackRespDTO> getExpressTrackList(Long id, Long userId) {
        // 查询订单
        TradeOrderDO order = tradeOrderMapper.selectByIdAndUserId(id, userId);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        // 查询物流
        return getExpressTrackList(order);
    }

    @Override
    public List<ExpressTrackRespDTO> getExpressTrackList(Long id) {
        // 查询订单
        TradeOrderDO order = tradeOrderMapper.selectById(id);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        // 查询物流
        return getExpressTrackList(order);
    }

    @Override
    public int getSeckillProductCount(Long userId, Long activityId) {
        // 获得订单列表
        List<TradeOrderDO> orders = tradeOrderMapper.selectListByUserIdAndSeckillActivityId(userId, activityId);
        orders.removeIf(order -> TradeOrderStatusEnum.isCanceled(order.getStatus())); // 过滤掉【已取消】的订单
        if (CollUtil.isEmpty(orders)) {
            return 0;
        }
        // 获得订单项列表
        return tradeOrderItemMapper.selectProductSumByOrderId(convertSet(orders, TradeOrderDO::getId));
    }

    /**
     * 获得订单的物流轨迹
     *
     * @param order 订单
     * @return 物流轨迹
     */
    private List<ExpressTrackRespDTO> getExpressTrackList(TradeOrderDO order) {
        if (order.getLogisticsId() == null) {
            return Collections.emptyList();
        }
        // 查询物流公司
        DeliveryExpressDO express = deliveryExpressService.getDeliveryExpress(order.getLogisticsId());
        if (express == null) {
            throw exception(EXPRESS_NOT_EXISTS);
        }
        // 查询物流轨迹
        return getSelf().getExpressTrackList(express.getCode(), order.getLogisticsNo(), order.getReceiverMobile());
    }

    /**
     * 查询物流轨迹
     * 加个 spring 缓存，30 分钟；主要考虑及时性要求不高，但是每次调用需要钱；TODO @艿艿：这个时间不会搞了。。。交给你了哈哈哈
     *
     * @param code           快递公司编码
     * @param logisticsNo    发货快递单号
     * @param receiverMobile 收、寄件人的电话号码
     * @return 物流轨迹
     */
    @Cacheable(cacheNames = RedisKeyConstants.EXPRESS_TRACK, key = "#code + '-' + #logisticsNo + '-' + #receiverMobile",
            condition = "#result != null")
    public List<ExpressTrackRespDTO> getExpressTrackList(String code, String logisticsNo, String receiverMobile) {
        // 查询物流轨迹
        return expressClientFactory.getDefaultExpressClient().getExpressTrackList(
                new ExpressTrackQueryReqDTO().setExpressCode(code).setLogisticsNo(logisticsNo)
                        .setPhone(receiverMobile));
    }


    // =================== Order Item ===================

    @Override
    public TradeOrderItemDO getOrderItem(Long userId, Long itemId) {
        TradeOrderItemDO orderItem = tradeOrderItemMapper.selectById(itemId);
        if (orderItem != null
                && ObjectUtil.notEqual(orderItem.getUserId(), userId)) {
            return null;
        }
        return orderItem;
    }

    @Override
    public TradeOrderItemDO getOrderItem(Long id) {
        return tradeOrderItemMapper.selectById(id);
    }

    @Override
    public List<TradeOrderItemDO> getOrderItemListByOrderId(Collection<Long> orderIds) {
        if (CollUtil.isEmpty(orderIds)) {
            return Collections.emptyList();
        }
        return tradeOrderItemMapper.selectListByOrderId(orderIds);
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private TradeOrderQueryServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
