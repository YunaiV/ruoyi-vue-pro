package cn.iocoder.yudao.module.trade.service.brokerage;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.math.Money;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.vo.record.BrokerageRecordPageReqVO;
import cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.user.AppBrokerageUserRankByPriceRespVO;
import cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.user.AppBrokerageUserRankPageReqVO;
import cn.iocoder.yudao.module.trade.convert.brokerage.BrokerageRecordConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.BrokerageRecordDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.BrokerageUserDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.config.TradeConfigDO;
import cn.iocoder.yudao.module.trade.dal.mysql.brokerage.BrokerageRecordMapper;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageRecordBizTypeEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageRecordStatusEnum;
import cn.iocoder.yudao.module.trade.service.brokerage.bo.BrokerageAddReqBO;
import cn.iocoder.yudao.module.trade.service.brokerage.bo.UserBrokerageSummaryBO;
import cn.iocoder.yudao.module.trade.service.config.TradeConfigService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.BROKERAGE_WITHDRAW_USER_BALANCE_NOT_ENOUGH;

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
                bizType, 1);

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
                bizType, 2);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBrokerage(Long userId, BrokerageRecordBizTypeEnum bizType, String bizId) {
        BrokerageRecordDO record = brokerageRecordMapper.selectByBizTypeAndBizIdAndUserId(bizType.getType(), bizId, userId);
        if (record == null) {
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
     * @param sourceUserLevel     来源用户等级
     */
    private void addBrokerage(BrokerageUserDO user, List<BrokerageAddReqBO> list, Integer brokerageFrozenDays,
                              Integer brokeragePercent, BrokerageRecordBizTypeEnum bizType, Integer sourceUserLevel) {
        // 1.1 处理冻结时间
        LocalDateTime unfreezeTime = null;
        if (brokerageFrozenDays != null && brokerageFrozenDays > 0) {
            unfreezeTime = LocalDateTime.now().plusDays(brokerageFrozenDays);
        }
        // 1.2 计算分佣
        int totalBrokerage = 0;
        List<BrokerageRecordDO> records = new ArrayList<>();
        for (BrokerageAddReqBO item : list) {
            // 计算金额
            Integer fixedPrice;
            if (Objects.equals(sourceUserLevel, 1)) {
                fixedPrice = item.getFirstFixedPrice();
            } else if (Objects.equals(sourceUserLevel, 2)) {
                fixedPrice = item.getSecondFixedPrice();
            } else {
                throw new IllegalArgumentException(StrUtil.format("用户等级({}) 不合法", sourceUserLevel));
            }
            int brokeragePrice = calculatePrice(item.getBasePrice(), brokeragePercent, fixedPrice);
            if (brokeragePrice <= 0) {
                continue;
            }
            totalBrokerage += brokeragePrice;
            // 创建记录实体
            records.add(BrokerageRecordConvert.INSTANCE.convert(user, bizType, item.getBizId(),
                    brokerageFrozenDays, brokeragePrice, unfreezeTime, item.getTitle(),
                    item.getSourceUserId(), sourceUserLevel));
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
    public UserBrokerageSummaryBO getUserBrokerageSummaryByUserId(Long userId, Integer bizType, Integer status) {
        UserBrokerageSummaryBO summaryBO = brokerageRecordMapper.selectCountAndSumPriceByUserIdAndBizTypeAndStatus(userId, bizType, status);
        return summaryBO != null ? summaryBO : new UserBrokerageSummaryBO(0, 0);
    }

    @Override
    public Integer getSummaryPriceByUserId(Long userId, Integer bizType, LocalDateTime beginTime, LocalDateTime endTime) {
        return brokerageRecordMapper.selectSummaryPriceByUserIdAndBizTypeAndCreateTimeBetween(userId, bizType, beginTime, endTime);
    }

    @Override
    public PageResult<AppBrokerageUserRankByPriceRespVO> getBrokerageUserChildSummaryPageByPrice(AppBrokerageUserRankPageReqVO pageReqVO) {
        IPage<AppBrokerageUserRankByPriceRespVO> pageResult = brokerageRecordMapper.selectSummaryPricePageGroupByUserId(MyBatisUtils.buildPage(pageReqVO),
                BrokerageRecordBizTypeEnum.ORDER.getType(), BrokerageRecordStatusEnum.SETTLEMENT.getStatus(),
                ArrayUtil.get(pageReqVO.getTimes(), 0), ArrayUtil.get(pageReqVO.getTimes(), 1));
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal());
    }

    @Override
    public Integer getUserRankByPrice(Long userId, LocalDateTime[] times) {
        AppBrokerageUserRankPageReqVO pageParam = new AppBrokerageUserRankPageReqVO().setTimes(times);
        // 取前100名
        pageParam.setPageSize(100);
        PageResult<AppBrokerageUserRankByPriceRespVO> pageResult = getBrokerageUserChildSummaryPageByPrice(pageParam);
        // 获得索引
        int index = CollUtil.indexOf(pageResult.getList(), user -> Objects.equals(userId, user.getId()));
        // 获得排名
        return index + 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBrokerage(Long userId, BrokerageRecordBizTypeEnum bizType, String bizId, Integer brokeragePrice, String title) {
        // 校验佣金余额
        BrokerageUserDO user = brokerageUserService.getBrokerageUser(userId);
        int balance = Optional.of(user)
                .map(BrokerageUserDO::getBrokeragePrice).orElse(0);
        if (balance + brokeragePrice < 0) {
            throw exception(BROKERAGE_WITHDRAW_USER_BALANCE_NOT_ENOUGH, new Money(0, balance));
        }

        // 扣减佣金余额
        boolean success = brokerageUserService.updateUserPrice(userId, brokeragePrice);
        if (!success) {
            throw exception(BROKERAGE_WITHDRAW_USER_BALANCE_NOT_ENOUGH, new Money(0, balance));
        }

        // 新增记录
        BrokerageRecordDO record = BrokerageRecordConvert.INSTANCE.convert(user, bizType, bizId, 0, brokeragePrice,
                null, title, null, null);
        brokerageRecordMapper.insert(record);
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
