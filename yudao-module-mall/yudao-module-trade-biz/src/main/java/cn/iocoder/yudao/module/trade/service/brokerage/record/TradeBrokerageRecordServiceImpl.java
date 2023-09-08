package cn.iocoder.yudao.module.trade.service.brokerage.record;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.record.vo.TradeBrokerageRecordPageReqVO;
import cn.iocoder.yudao.module.trade.convert.brokerage.record.TradeBrokerageRecordConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.record.TradeBrokerageRecordDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.user.TradeBrokerageUserDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.config.TradeConfigDO;
import cn.iocoder.yudao.module.trade.dal.mysql.brokerage.record.TradeBrokerageRecordMapper;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageRecordBizTypeEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageRecordStatusEnum;
import cn.iocoder.yudao.module.trade.service.brokerage.record.bo.BrokerageAddReqBO;
import cn.iocoder.yudao.module.trade.service.brokerage.record.bo.UserBrokerageSummaryBO;
import cn.iocoder.yudao.module.trade.service.brokerage.user.TradeBrokerageUserService;
import cn.iocoder.yudao.module.trade.service.config.TradeConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 佣金记录 Service 实现类
 *
 * @author owen
 */
@Slf4j
@Service
@Validated
public class TradeBrokerageRecordServiceImpl implements TradeBrokerageRecordService {

    @Resource
    private TradeBrokerageRecordMapper tradeBrokerageRecordMapper;
    @Resource
    private TradeConfigService tradeConfigService;
    @Resource
    private TradeBrokerageUserService tradeBrokerageUserService;

    @Override
    public TradeBrokerageRecordDO getBrokerageRecord(Integer id) {
        return tradeBrokerageRecordMapper.selectById(id);
    }

    @Override
    public PageResult<TradeBrokerageRecordDO> getBrokerageRecordPage(TradeBrokerageRecordPageReqVO pageReqVO) {
        return tradeBrokerageRecordMapper.selectPage(pageReqVO);
    }

    // TODO @疯狂：buyerId 要不要统一改成 userId 哈；
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBrokerage(Long buyerId, List<BrokerageAddReqBO> list) {
        TradeConfigDO memberConfig = tradeConfigService.getTradeConfig();
        // 0 未启用分销功能
        // TODO @疯狂：BooleanUtil.isFalse()；逻辑里，尽量不做 !取反，这样要多思考一层；
        if (memberConfig == null || !BooleanUtil.isTrue(memberConfig.getBrokerageEnabled())) {
            log.warn("[addBrokerage][增加佣金失败：brokerageEnabled 未配置，buyerId({})", buyerId);
            return;
        }

        // 1.1 获得一级推广人
        TradeBrokerageUserDO firstUser = tradeBrokerageUserService.getInviteBrokerageUser(buyerId);
        if (firstUser == null || !BooleanUtil.isTrue(firstUser.getBrokerageEnabled())) {
            return;
        }

        // 1.2 计算一级分佣 // TODO 疯狂：类似 1.1 和 1.2 的空行，可以去掉；一般在代码里的空行，是为了逻辑分块；但是分块如果太多，就会导致代码里都是空行哈；
        addBrokerage(firstUser, list, memberConfig.getBrokerageFrozenDays(), memberConfig.getBrokerageFirstPercent(), BrokerageAddReqBO::getSkuFirstBrokeragePrice);

        // 2.1 获得二级推广员
        // TODO @疯狂：这里可以加个 firstUser.getBrokerageUserId() 为空的判断 return
        TradeBrokerageUserDO secondUser = tradeBrokerageUserService.getBrokerageUser(firstUser.getBrokerageUserId());
        if (secondUser == null || !BooleanUtil.isTrue(secondUser.getBrokerageEnabled())) {
            return;
        }

        // 2.2 计算二级分佣
        addBrokerage(secondUser, list, memberConfig.getBrokerageFrozenDays(), memberConfig.getBrokerageSecondPercent(), BrokerageAddReqBO::getSkuSecondBrokeragePrice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBrokerage(Long userId, String bizId) {
        TradeBrokerageRecordDO record = tradeBrokerageRecordMapper.selectByUserIdAndBizTypeAndBizId(BrokerageRecordBizTypeEnum.ORDER.getType(), bizId);
        if (record == null || ObjectUtil.notEqual(record.getUserId(), userId)) {
            log.error("[cancelBrokerage][userId({})][bizId({}) 更新为已失效失败：记录不存在]", userId, bizId);
            return;
        }

        // 1. 更新佣金记录为已失效
        TradeBrokerageRecordDO updateObj = new TradeBrokerageRecordDO().setStatus(BrokerageRecordStatusEnum.CANCEL.getStatus());
        int updateRows = tradeBrokerageRecordMapper.updateByIdAndStatus(record.getId(), record.getStatus(), updateObj);
        if (updateRows == 0) {
            log.error("[cancelBrokerage][record({}) 更新为已失效失败]", record.getId());
            return;
        }

        // 2. 更新用户的佣金
        if (BrokerageRecordStatusEnum.WAIT_SETTLEMENT.getStatus().equals(record.getStatus())) {
            tradeBrokerageUserService.updateUserFrozenBrokeragePrice(userId, -record.getPrice());
        } else if (BrokerageRecordStatusEnum.SETTLEMENT.getStatus().equals(record.getStatus())) {
            tradeBrokerageUserService.updateUserBrokeragePrice(userId, -record.getPrice());
        }
    }

    // TODO @疯狂：是不是 calculateBrokeragePrice
    /**
     * 计算佣金
     *
     * @param payPrice          订单支付金额
     * @param percent           商品 SKU 设置的佣金
     * @param skuBrokeragePrice 商品的佣金
     * @return 佣金
     */
    int calculateBrokerage(Integer payPrice, Integer percent, Integer skuBrokeragePrice) {
        // 1. 优先使用商品 SKU 设置的佣金
        if (skuBrokeragePrice != null && skuBrokeragePrice > 0) {
            return ObjectUtil.defaultIfNull(skuBrokeragePrice, 0);
        }
        // 2. 根据订单支付金额计算佣金
        // TODO @疯狂：要不要把 MoneyUtils 抽到 common 里，然后这里也使用这个类的方法；
        if (payPrice != null && payPrice > 0 && percent != null && percent > 0) {
            return NumberUtil.div(NumberUtil.mul(payPrice, percent), 100, 0, RoundingMode.DOWN).intValue();
        }
        return 0;
    }

    /**
     * 增加用户佣金
     *
     * @param user                 用户
     * @param list                 佣金增加参数列表
     * @param brokerageFrozenDays  冻结天数
     * @param brokeragePercent     佣金比例
     * @param skuBrokeragePriceFun 商品 SKU 设置的佣金
     */
    private void addBrokerage(TradeBrokerageUserDO user, List<BrokerageAddReqBO> list, Integer brokerageFrozenDays,
                              Integer brokeragePercent, Function<BrokerageAddReqBO, Integer> skuBrokeragePriceFun) {
        // 1.1 处理冻结时间
        // TODO @疯狂：是不是 brokerageFrozenDays !=  null && brokerageFrozenDays > 0 ？然后计算；更简洁一点；
        brokerageFrozenDays = ObjectUtil.defaultIfNull(brokerageFrozenDays, 0);
        LocalDateTime unfreezeTime = null;
        if (brokerageFrozenDays > 0) {
            unfreezeTime = LocalDateTime.now().plusDays(brokerageFrozenDays);
        }
        // 1.2 计算分佣
        int totalBrokerage = 0;
        List<TradeBrokerageRecordDO> records = new ArrayList<>();
        for (BrokerageAddReqBO item : list) {
            int brokeragePerItem = calculateBrokerage(item.getPayPrice(), brokeragePercent, skuBrokeragePriceFun.apply(item));
            // TODO @疯狂：其实可以 brokeragePerItem <= 0 ，continue；这样 { 层级更少；代码更简洁；}
            if (brokeragePerItem > 0) {
                int brokerage = brokeragePerItem * item.getCount();
                records.add(TradeBrokerageRecordConvert.INSTANCE.convert(user, item.getBizId(), brokerageFrozenDays, brokerage, unfreezeTime));
                totalBrokerage += brokerage;
            }
        }
        if (CollUtil.isEmpty(records)) {
            return;
        }
        // 1.3 保存佣金记录
        tradeBrokerageRecordMapper.insertBatch(records);

        // 2. 更新用户佣金
        if (brokerageFrozenDays > 0) { // 更新用户冻结佣金
            tradeBrokerageUserService.updateUserFrozenBrokeragePrice(user.getId(), totalBrokerage);
        } else { // 更新用户可用佣金
            tradeBrokerageUserService.updateUserBrokeragePrice(user.getId(), totalBrokerage);
        }
    }

    @Override
    public int unfreezeRecord() {
        // 1. 查询待结算的佣金记录
        List<TradeBrokerageRecordDO> records = tradeBrokerageRecordMapper.selectListByStatusAndUnfreezeTimeLt(
                BrokerageRecordStatusEnum.WAIT_SETTLEMENT.getStatus(), LocalDateTime.now());
        if (CollUtil.isEmpty(records)) {
            return 0;
        }

        // 2. 遍历执行
        int count = 0;
        for (TradeBrokerageRecordDO record : records) {
            try {
                boolean success = getSelf().unfreezeRecord(record);
                if (success) {
                    count++;
                }
            } catch (Exception e) {
                log.error("[unfreezeRecord][record({}) 更新为已结算失败]", record.getId(), e);
            }
        }
        return count;
    }

    @Override
    public UserBrokerageSummaryBO summaryByUserIdAndBizTypeAndStatus(Long userId, Integer bizType, Integer status) {
        UserBrokerageSummaryBO summaryBO = tradeBrokerageRecordMapper.selectCountAndSumPriceByUserIdAndBizTypeAndStatus(userId, bizType, status);
        return summaryBO == null ? new UserBrokerageSummaryBO(0, 0) : summaryBO;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean unfreezeRecord(TradeBrokerageRecordDO record) {
        // 更新记录状态
        TradeBrokerageRecordDO updateObj = new TradeBrokerageRecordDO()
                .setStatus(BrokerageRecordStatusEnum.SETTLEMENT.getStatus())
                .setUnfreezeTime(LocalDateTime.now());
        int updateRows = tradeBrokerageRecordMapper.updateByIdAndStatus(record.getId(), record.getStatus(), updateObj);
        if (updateRows == 0) {
            log.error("[unfreezeRecord][record({}) 更新为已结算失败]", record.getId());
            return false;
        }

        // 更新用户冻结佣金
        tradeBrokerageUserService.updateFrozenBrokeragePriceDecrAndBrokeragePriceIncr(record.getUserId(), -record.getPrice());
        log.info("[unfreezeRecord][record({}) 更新为已结算成功]", record.getId());
        return true;
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private TradeBrokerageRecordServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
