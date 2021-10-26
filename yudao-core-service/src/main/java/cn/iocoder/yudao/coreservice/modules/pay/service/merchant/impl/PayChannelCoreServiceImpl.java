package cn.iocoder.yudao.coreservice.modules.pay.service.merchant.impl;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayChannelDO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.mysql.merchant.PayChannelCoreMapper;
import cn.iocoder.yudao.coreservice.modules.pay.enums.PayErrorCodeCoreConstants;
import cn.iocoder.yudao.coreservice.modules.pay.service.merchant.PayChannelCoreService;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.pay.core.client.PayClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.Valid;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static cn.iocoder.yudao.coreservice.modules.pay.enums.PayErrorCodeCoreConstants.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 支付渠道 Core Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Valid
@Slf4j
public class PayChannelCoreServiceImpl implements PayChannelCoreService {

    /**
     * 定时执行 {@link #schedulePeriodicRefresh()} 的周期
     * 因为已经通过 Redis Pub/Sub 机制，所以频率不需要高
     */
    private static final long SCHEDULER_PERIOD = 5 * 60 * 1000L;

    /**
     * 缓存菜单的最大更新时间，用于后续的增量轮询，判断是否有更新
     */
    private volatile Date maxUpdateTime;

    @Resource
    private PayChannelCoreMapper payChannelCoreMapper;

    @Resource
    private PayClientFactory payClientFactory;

    @Override
    @PostConstruct
    public void initPayClients() {
        // 获取支付渠道，如果有更新
        List<PayChannelDO> payChannels = this.loadPayChannelIfUpdate(maxUpdateTime);
        if (CollUtil.isEmpty(payChannels)) {
            return;
        }

        // 创建或更新支付 Client
        payChannels.forEach(payChannel -> payClientFactory.createOrUpdatePayClient(payChannel.getId(),
                payChannel.getCode(), payChannel.getConfig()));

        // 写入缓存
        assert payChannels.size() > 0; // 断言，避免告警
        maxUpdateTime = payChannels.stream().max(Comparator.comparing(BaseDO::getUpdateTime)).get().getUpdateTime();
        log.info("[initPayClients][初始化 PayChannel 数量为 {}]", payChannels.size());
    }

    @Scheduled(fixedDelay = SCHEDULER_PERIOD, initialDelay = SCHEDULER_PERIOD)
    public void schedulePeriodicRefresh() {
        initPayClients();
    }

    /**
     * 如果支付渠道发生变化，从数据库中获取最新的全量支付渠道。
     * 如果未发生变化，则返回空
     *
     * @param maxUpdateTime 当前支付渠道的最大更新时间
     * @return 支付渠道列表
     */
    private List<PayChannelDO> loadPayChannelIfUpdate(Date maxUpdateTime) {
        // 第一步，判断是否要更新。
        if (maxUpdateTime == null) { // 如果更新时间为空，说明 DB 一定有新数据
            log.info("[loadPayChannelIfUpdate][首次加载全量支付渠道]");
        } else { // 判断数据库中是否有更新的支付渠道
            if (payChannelCoreMapper.selectExistsByUpdateTimeAfter(maxUpdateTime) == null) {
                return null;
            }
            log.info("[loadPayChannelIfUpdate][增量加载全量支付渠道]");
        }
        // 第二步，如果有更新，则从数据库加载所有支付渠道
        return payChannelCoreMapper.selectList();
    }

    @Override
    public PayChannelDO validPayChannel(Long id) {
        PayChannelDO channel = payChannelCoreMapper.selectById(id);
        this.validPayChannel(channel);
        return channel;
    }

    @Override
    public PayChannelDO validPayChannel(Long appId, String code) {
        PayChannelDO channel = payChannelCoreMapper.selectByAppIdAndCode(appId, code);
        this.validPayChannel(channel);
        return channel;
    }

    private void validPayChannel(PayChannelDO channel) {
        if (channel == null) {
            throw exception(PAY_CHANNEL_NOT_FOUND);
        }
        if (CommonStatusEnum.DISABLE.getStatus().equals(channel.getStatus())) {
            throw exception(PayErrorCodeCoreConstants.PAY_CHANNEL_IS_DISABLE);
        }
    }

}
