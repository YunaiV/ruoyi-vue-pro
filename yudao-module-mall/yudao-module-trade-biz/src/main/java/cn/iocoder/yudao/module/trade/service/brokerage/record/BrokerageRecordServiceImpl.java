package cn.iocoder.yudao.module.trade.service.brokerage.record;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.record.vo.BrokerageRecordPageReqVO;
import cn.iocoder.yudao.module.trade.convert.brokerage.record.BrokerageRecordConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.record.BrokerageRecordDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.user.BrokerageUserDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.config.TradeConfigDO;
import cn.iocoder.yudao.module.trade.dal.mysql.brokerage.record.BrokerageRecordMapper;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageRecordBizTypeEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageRecordStatusEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageUserTypeEnum;
import cn.iocoder.yudao.module.trade.service.brokerage.bo.BrokerageAddReqBO;
import cn.iocoder.yudao.module.trade.service.brokerage.bo.UserBrokerageSummaryBO;
import cn.iocoder.yudao.module.trade.service.brokerage.user.BrokerageUserService;
import cn.iocoder.yudao.module.trade.service.config.TradeConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 佣金记录 Service 实现类
 *
 * @author owen
 */
@Slf4j
@Service
@Validated
public class BrokerageRecordServiceImpl implements BrokerageRecordService {

    @Resource
    private BrokerageRecordMapper brokerageRecordMapper;
    @Resource
    private TradeConfigService tradeConfigService;
    @Resource
    private BrokerageUserService brokerageUserService;

    @Override
    public BrokerageRecordDO getBrokerageRecord(Integer id) {
        return brokerageRecordMapper.selectById(id);
    }

    @Override
    public PageResult<BrokerageRecordDO> getBrokerageRecordPage(BrokerageRecordPageReqVO pageReqVO) {
        return brokerageRecordMapper.selectPage(pageReqVO);
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
        BrokerageUserDO firstUser = brokerageUserService.getBindBrokerageUser(userId);
        if (firstUser == null || !BooleanUtil.isTrue(firstUser.getBrokerageEnabled())) {
            return;
        }
        // 1.2 计算一级分佣
        addBrokerage(firstUser, list, memberConfig.getBrokerageFrozenDays(), memberConfig.getBrokerageFirstPercent(),
                bizType, BrokerageUserTypeEnum.FIRST);

        // 2.1 获得二级推广员
        if (firstUser.getBindUserId() == null) {
            return;
        }
        BrokerageUserDO secondUser = brokerageUserService.getBrokerageUser(firstUser.getBindUserId());
        if (secondUser == null || !BooleanUtil.isTrue(secondUser.getBrokerageEnabled())) {
            return;
        }
        // 2.2 计算二级分佣
        addBrokerage(secondUser, list, memberConfig.getBrokerageFrozenDays(), memberConfig.getBrokerageSecondPercent(),
                bizType, BrokerageUserTypeEnum.SECOND);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBrokerage(Long userId, BrokerageRecordBizTypeEnum bizType, String bizId) {
        BrokerageRecordDO record = brokerageRecordMapper.selectByBizTypeAndBizId(bizType.getType(), bizId);
        if (record == null || ObjectUtil.notEqual(record.getUserId(), userId)) {
            log.error("[cancelBrokerage][userId({})][bizId({}) 更新为已失效失败：记录不存在]", userId, bizId);
            return;
        }

        // 1. 更新佣金记录为已失效
        BrokerageRecordDO updateObj = new BrokerageRecordDO().setStatus(BrokerageRecordStatusEnum.CANCEL.getStatus());
        int updateRows = brokerageRecordMapper.updateByIdAndStatus(record.getId(), record.getStatus(), updateObj);
        if (updateRows == 0) {
            log.error("[cancelBrokerage][record({}) 更新为已失效失败]", record.getId());
            return;
        }

        // 2. 更新用户的佣金
        if (BrokerageRecordStatusEnum.WAIT_SETTLEMENT.getStatus().equals(record.getStatus())) {
            brokerageUserService.updateUserFrozenPrice(userId, -record.getPrice());
        } else if (BrokerageRecordStatusEnum.SETTLEMENT.getStatus().equals(record.getStatus())) {
            brokerageUserService.updateUserPrice(userId, -record.getPrice());
        }
    }

    /**
     * 计算佣金
     *
     * @param basePrice  佣金基数
     * @param percent    佣金比例
     * @param fixedPrice 固定佣金
     * @return 佣金
     */
    int calculatePrice(Integer basePrice, Integer percent, Integer fixedPrice) {
        // 1. 优先使用固定佣金
        if (fixedPrice != null && fixedPrice > 0) {
            return ObjectUtil.defaultIfNull(fixedPrice, 0);
        }
        // 2. 根据比例计算佣金
        if (basePrice != null && basePrice > 0 && percent != null && percent > 0) {
            return MoneyUtils.calculateRatePriceFloor(basePrice, Double.valueOf(percent));
        }
        return 0;
    }

    /**
     * 增加用户佣金
     *
     * @param user                用户
     * @param list                佣金增加参数列表
     * @param brokerageFrozenDays 冻结天数
     * @param brokeragePercent    佣金比例
     * @param bizType             业务类型
     * @param sourceUserType      来源用户类型
     */
    private void addBrokerage(BrokerageUserDO user, List<BrokerageAddReqBO> list, Integer brokerageFrozenDays,
                              Integer brokeragePercent, BrokerageRecordBizTypeEnum bizType, BrokerageUserTypeEnum sourceUserType) {
        // 1.1 处理冻结时间
        LocalDateTime unfreezeTime = null;
        if (brokerageFrozenDays != null && brokerageFrozenDays > 0) {
            unfreezeTime = LocalDateTime.now().plusDays(brokerageFrozenDays);
        }
        // 1.2 计算分佣
        int totalBrokerage = 0;
        List<BrokerageRecordDO> records = new ArrayList<>();
        for (BrokerageAddReqBO item : list) {
            Integer fixedPrice = 0;
            if (BrokerageUserTypeEnum.FIRST.equals(sourceUserType)) {
                fixedPrice = item.getFirstFixedPrice();
            } else if (BrokerageUserTypeEnum.SECOND.equals(sourceUserType)) {
                fixedPrice = item.getSecondFixedPrice();
            }

            int brokeragePerItem = calculatePrice(item.getBasePrice(), brokeragePercent, fixedPrice);
            if (brokeragePerItem <= 0) {
                continue;
            }
            records.add(BrokerageRecordConvert.INSTANCE.convert(user, bizType, item.getBizId(),
                    brokerageFrozenDays, brokeragePerItem, unfreezeTime, bizType.getTitle(),
                    item.getSourceUserId(), sourceUserType.getType()));
            totalBrokerage += brokeragePerItem;
        }
        if (CollUtil.isEmpty(records)) {
            return;
        }
        // 1.3 保存佣金记录
        brokerageRecordMapper.insertBatch(records);

        // 2. 更新用户佣金
        if (brokerageFrozenDays != null && brokerageFrozenDays > 0) { // 更新用户冻结佣金
            brokerageUserService.updateUserFrozenPrice(user.getId(), totalBrokerage);
        } else { // 更新用户可用佣金
            brokerageUserService.updateUserPrice(user.getId(), totalBrokerage);
        }
    }

    @Override
    public int unfreezeRecord() {
        // 1. 查询待结算的佣金记录
        List<BrokerageRecordDO> records = brokerageRecordMapper.selectListByStatusAndUnfreezeTimeLt(
                BrokerageRecordStatusEnum.WAIT_SETTLEMENT.getStatus(), LocalDateTime.now());
        if (CollUtil.isEmpty(records)) {
            return 0;
        }

        // 2. 遍历执行
        int count = 0;
        for (BrokerageRecordDO record : records) {
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
        UserBrokerageSummaryBO summaryBO = brokerageRecordMapper.selectCountAndSumPriceByUserIdAndBizTypeAndStatus(userId, bizType, status);
        return summaryBO == null ? new UserBrokerageSummaryBO(0, 0) : summaryBO;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean unfreezeRecord(BrokerageRecordDO record) {
        // 更新记录状态
        BrokerageRecordDO updateObj = new BrokerageRecordDO()
                .setStatus(BrokerageRecordStatusEnum.SETTLEMENT.getStatus())
                .setUnfreezeTime(LocalDateTime.now());
        int updateRows = brokerageRecordMapper.updateByIdAndStatus(record.getId(), record.getStatus(), updateObj);
        if (updateRows == 0) {
            log.error("[unfreezeRecord][record({}) 更新为已结算失败]", record.getId());
            return false;
        }

        // 更新用户冻结佣金
        brokerageUserService.updateFrozenPriceDecrAndPriceIncr(record.getUserId(), -record.getPrice());
        log.info("[unfreezeRecord][record({}) 更新为已结算成功]", record.getId());
        return true;
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private BrokerageRecordServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
