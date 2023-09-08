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
import cn.iocoder.yudao.module.trade.service.brokerage.bo.BrokerageAddReqBO;
import cn.iocoder.yudao.module.trade.service.brokerage.bo.UserBrokerageSummaryBO;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBrokerage(Long userId, BrokerageRecordBizTypeEnum bizType, List<BrokerageAddReqBO> list) {
        TradeConfigDO memberConfig = tradeConfigService.getTradeConfig();
        // 0 未启用分销功能
        if (memberConfig == null || !BooleanUtil.isTrue(memberConfig.getBrokerageEnabled())) {
            log.warn("[addBrokerage][增加佣金失败：brokerageEnabled 未配置，userId({})", userId);
            return;
        }

        // 1.1 获得一级推广人
        TradeBrokerageUserDO firstUser = tradeBrokerageUserService.getBindBrokerageUser(userId);
        if (firstUser == null || !BooleanUtil.isTrue(firstUser.getBrokerageEnabled())) {
            return;
        }
        // 1.2 计算一级分佣
        addBrokerage(firstUser, list, memberConfig.getBrokerageFrozenDays(), memberConfig.getBrokerageFirstPercent(), BrokerageAddReqBO::getFirstBrokeragePrice, bizType);

        // 2.1 获得二级推广员
        if (firstUser.getBrokerageUserId() == null) {
            return;
        }
        TradeBrokerageUserDO secondUser = tradeBrokerageUserService.getBrokerageUser(firstUser.getBrokerageUserId());
        if (secondUser == null || !BooleanUtil.isTrue(secondUser.getBrokerageEnabled())) {
            return;
        }
        // 2.2 计算二级分佣
        addBrokerage(secondUser, list, memberConfig.getBrokerageFrozenDays(), memberConfig.getBrokerageSecondPercent(), BrokerageAddReqBO::getSecondBrokeragePrice, bizType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBrokerage(Long userId, BrokerageRecordBizTypeEnum bizType, String bizId) {
        TradeBrokerageRecordDO record = tradeBrokerageRecordMapper.selectByBizTypeAndBizId(bizType.getType(), bizId);
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

    /**
     * 计算佣金
     *
     * @param basePrice           佣金基数
     * @param percent             佣金比例
     * @param fixedBrokeragePrice 固定佣金
     * @return 佣金
     */
    int calculateBrokeragePrice(Integer basePrice, Integer percent, Integer fixedBrokeragePrice) {
        // 1. 优先使用固定佣金
        if (fixedBrokeragePrice != null && fixedBrokeragePrice > 0) {
            return ObjectUtil.defaultIfNull(fixedBrokeragePrice, 0);
        }
        // 2. 根据比例计算佣金
        // TODO @疯狂：要不要把 MoneyUtils 抽到 common 里，然后这里也使用这个类的方法；
        if (basePrice != null && basePrice > 0 && percent != null && percent > 0) {
            return NumberUtil.div(NumberUtil.mul(basePrice, percent), 100, 0, RoundingMode.DOWN).intValue();
        }
        return 0;
    }

    /**
     * 增加用户佣金
     *
     * @param user                   用户
     * @param list                   佣金增加参数列表
     * @param brokerageFrozenDays    冻结天数
     * @param brokeragePercent       佣金比例
     * @param FixedBrokeragePriceFun 固定佣金
     * @param bizType                业务类型
     */
    private void addBrokerage(TradeBrokerageUserDO user, List<BrokerageAddReqBO> list, Integer brokerageFrozenDays,
                              Integer brokeragePercent, Function<BrokerageAddReqBO, Integer> FixedBrokeragePriceFun,
                              BrokerageRecordBizTypeEnum bizType) {
        // 1.1 处理冻结时间
        LocalDateTime unfreezeTime = null;
        if (brokerageFrozenDays != null && brokerageFrozenDays > 0) {
            unfreezeTime = LocalDateTime.now().plusDays(brokerageFrozenDays);
        }
        // 1.2 计算分佣
        int totalBrokerage = 0;
        List<TradeBrokerageRecordDO> records = new ArrayList<>();
        for (BrokerageAddReqBO item : list) {
            int brokeragePerItem = calculateBrokeragePrice(item.getBasePrice(), brokeragePercent, FixedBrokeragePriceFun.apply(item));
            if (brokeragePerItem <= 0) {
                continue;
            }
            records.add(TradeBrokerageRecordConvert.INSTANCE.convert(user, bizType, item.getBizId(),
                    brokerageFrozenDays, brokeragePerItem, unfreezeTime, bizType.getTitle()));
            totalBrokerage += brokeragePerItem;
        }
        if (CollUtil.isEmpty(records)) {
            return;
        }
        // 1.3 保存佣金记录
        tradeBrokerageRecordMapper.insertBatch(records);

        // 2. 更新用户佣金
        if (brokerageFrozenDays != null && brokerageFrozenDays > 0) { // 更新用户冻结佣金
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
