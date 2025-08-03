package cn.iocoder.yudao.module.trade.service.brokerage;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.*;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.vo.record.BrokerageRecordPageReqVO;
import cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.record.AppBrokerageProductPriceRespVO;
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
import cn.iocoder.yudao.module.trade.service.brokerage.bo.UserBrokerageSummaryRespBO;
import cn.iocoder.yudao.module.trade.service.config.TradeConfigService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.getMaxValue;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.getMinValue;
import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getLoginUserId;
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

    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private ProductSkuApi productSkuApi;

    @Override
    public BrokerageRecordDO getBrokerageRecord(Long id) {
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
            log.error("[addBrokerage][增加佣金失败：brokerageEnabled 未配置，userId({}) bizType({}) list({})", userId, bizType, list);
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
    public void cancelBrokerage(BrokerageRecordBizTypeEnum bizType, String bizId) {
        List<BrokerageRecordDO> records = brokerageRecordMapper.selectListByBizTypeAndBizId(bizType.getType(), bizId);
        if (CollUtil.isEmpty(records)) {
            log.error("[cancelBrokerage][bizId({}) bizType({}) 更新为已失效失败：记录不存在]", bizId, bizType);
            return;
        }

        records.forEach(record -> {
            // 1. 更新佣金记录为已失效
            BrokerageRecordDO updateObj = new BrokerageRecordDO().setStatus(BrokerageRecordStatusEnum.CANCEL.getStatus());
            int updateRows = brokerageRecordMapper.updateByIdAndStatus(record.getId(), record.getStatus(), updateObj);
            if (updateRows == 0) {
                log.error("[cancelBrokerage][record({}) 更新为已失效失败]", record.getId());
                return;
            }

            // 2. 更新用户的佣金
            if (BrokerageRecordStatusEnum.WAIT_SETTLEMENT.getStatus().equals(record.getStatus())) {
                brokerageUserService.updateUserFrozenPrice(record.getUserId(), -record.getPrice());
            } else if (BrokerageRecordStatusEnum.SETTLEMENT.getStatus().equals(record.getStatus())) {
                brokerageUserService.updateUserPrice(record.getUserId(), -record.getPrice());
            }
        });
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

    /**
     * 解冻单条佣金记录
     *
     * @param record 佣金记录
     * @return 解冻是否成功
     */
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

    @Override
    public List<UserBrokerageSummaryRespBO> getUserBrokerageSummaryListByUserId(Collection<Long> userIds,
                                                                                Integer bizType, Integer status) {
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        return brokerageRecordMapper.selectCountAndSumPriceByUserIdInAndBizTypeAndStatus(userIds, bizType, status);
    }

    @Override
    public Integer getSummaryPriceByUserId(Long userId, BrokerageRecordBizTypeEnum bizType, BrokerageRecordStatusEnum status,
                                           LocalDateTime beginTime, LocalDateTime endTime) {
        return brokerageRecordMapper.selectSummaryPriceByUserIdAndBizTypeAndCreateTimeBetween(userId,
                bizType.getType(), status.getStatus(), beginTime, endTime);
    }

    @Override
    public PageResult<AppBrokerageUserRankByPriceRespVO> getBrokerageUserChildSummaryPageByPrice(AppBrokerageUserRankPageReqVO pageReqVO) {
        IPage<AppBrokerageUserRankByPriceRespVO> pageResult = brokerageRecordMapper.selectSummaryPricePageGroupByUserId(
                MyBatisUtils.buildPage(pageReqVO),
                BrokerageRecordBizTypeEnum.ORDER.getType(), BrokerageRecordStatusEnum.SETTLEMENT.getStatus(),
                ArrayUtil.get(pageReqVO.getTimes(), 0), ArrayUtil.get(pageReqVO.getTimes(), 1));
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal());
    }

    @Override
    public Integer getUserRankByPrice(Long userId, LocalDateTime[] times) {
        // 用户的推广金额
        Integer price = brokerageRecordMapper.selectSummaryPriceByUserIdAndBizTypeAndCreateTimeBetween(userId,
                BrokerageRecordBizTypeEnum.ORDER.getType(), BrokerageRecordStatusEnum.SETTLEMENT.getStatus(),
                ArrayUtil.get(times, 0), ArrayUtil.get(times, 1));
        // 排在用户前面的人数
        Integer greaterCount = brokerageRecordMapper.selectCountByPriceGt(price,
                BrokerageRecordBizTypeEnum.ORDER.getType(), BrokerageRecordStatusEnum.SETTLEMENT.getStatus(),
                ArrayUtil.get(times, 0), ArrayUtil.get(times, 1));
        // 获得排名
        return ObjUtil.defaultIfNull(greaterCount, 0) + 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBrokerage(Long userId, BrokerageRecordBizTypeEnum bizType, String bizId, Integer brokeragePrice, String title) {
        // 1. 校验佣金余额
        BrokerageUserDO user = brokerageUserService.getBrokerageUser(userId);
        int balance = Optional.of(user)
                .map(BrokerageUserDO::getBrokeragePrice).orElse(0);
        if (balance + brokeragePrice < 0) {
            throw exception(BROKERAGE_WITHDRAW_USER_BALANCE_NOT_ENOUGH, MoneyUtils.fenToYuanStr(balance));
        }

        // 2. 更新佣金余额
        boolean success = brokerageUserService.updateUserPrice(userId, brokeragePrice);
        if (!success) {
            // 失败时，则抛出异常。只会出现扣减佣金时，余额不足的情况
            throw exception(BROKERAGE_WITHDRAW_USER_BALANCE_NOT_ENOUGH, MoneyUtils.fenToYuanStr(balance));
        }

        // 3. 新增记录
        BrokerageRecordDO record = BrokerageRecordConvert.INSTANCE.convert(user, bizType, bizId, 0, brokeragePrice,
                null, title, null, null);
        brokerageRecordMapper.insert(record);
    }

    @Override
    public AppBrokerageProductPriceRespVO calculateProductBrokeragePrice(Long userId, Long spuId) {
        // 1. 构建默认的返回值
        AppBrokerageProductPriceRespVO respVO = new AppBrokerageProductPriceRespVO().setEnabled(false)
                .setBrokerageMinPrice(0).setBrokerageMaxPrice(0);

        // 2.1 校验分销功能是否开启
        TradeConfigDO tradeConfig = tradeConfigService.getTradeConfig();
        if (tradeConfig == null || BooleanUtil.isFalse(tradeConfig.getBrokerageEnabled())) {
            return respVO;
        }
        // 2.2 校验用户是否有分销资格
        respVO.setEnabled(brokerageUserService.getUserBrokerageEnabled(getLoginUserId()));
        if (BooleanUtil.isFalse(respVO.getEnabled())) {
            return respVO;
        }
        // 2.3 校验商品是否存在
        ProductSpuRespDTO spu = productSpuApi.getSpu(spuId);
        if (spu == null) {
            return respVO;
        }

        // 3.1 商品单独分佣模式
        Integer fixedMinPrice = 0;
        Integer fixedMaxPrice = 0;
        Integer spuMinPrice = 0;
        Integer spuMaxPrice = 0;
        List<ProductSkuRespDTO> skuList = productSkuApi.getSkuListBySpuId(ListUtil.of(spuId));
        if (BooleanUtil.isTrue(spu.getSubCommissionType())) {
            fixedMinPrice = getMinValue(skuList, ProductSkuRespDTO::getFirstBrokeragePrice);
            fixedMaxPrice = getMaxValue(skuList, ProductSkuRespDTO::getFirstBrokeragePrice);
            // 3.2 全局分佣模式（根据商品价格比例计算）
        } else {
            spuMinPrice = getMinValue(skuList, ProductSkuRespDTO::getPrice);
            spuMaxPrice = getMaxValue(skuList, ProductSkuRespDTO::getPrice);
        }
        respVO.setBrokerageMinPrice(calculatePrice(spuMinPrice, tradeConfig.getBrokerageFirstPercent(), fixedMinPrice));
        respVO.setBrokerageMaxPrice(calculatePrice(spuMaxPrice, tradeConfig.getBrokerageFirstPercent(), fixedMaxPrice));
        return respVO;
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
