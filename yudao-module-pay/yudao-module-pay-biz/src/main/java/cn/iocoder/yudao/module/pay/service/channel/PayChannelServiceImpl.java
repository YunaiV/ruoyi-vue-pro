package cn.iocoder.yudao.module.pay.service.channel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.pay.core.client.PayClientConfig;
import cn.iocoder.yudao.framework.pay.core.client.PayClientFactory;
import cn.iocoder.yudao.framework.pay.core.client.impl.AbstractPayClient;
import cn.iocoder.yudao.framework.pay.core.client.impl.NonePayClientConfig;
import cn.iocoder.yudao.framework.pay.core.client.impl.alipay.*;
import cn.iocoder.yudao.framework.pay.core.client.impl.mock.MockPayClient;
import cn.iocoder.yudao.framework.pay.core.client.impl.mock.MockPayClientConfig;
import cn.iocoder.yudao.framework.pay.core.client.impl.weixin.*;
import cn.iocoder.yudao.framework.pay.core.enums.channel.PayChannelEnum;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.pay.controller.admin.channel.vo.PayChannelCreateReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.channel.vo.PayChannelUpdateReqVO;
import cn.iocoder.yudao.module.pay.convert.channel.PayChannelConvert;
import cn.iocoder.yudao.module.pay.dal.dataobject.channel.PayChannelDO;
import cn.iocoder.yudao.module.pay.dal.mysql.channel.PayChannelMapper;
import cn.iocoder.yudao.module.pay.framework.pay.wallet.WalletPayClient;
import cn.iocoder.yudao.module.pay.service.wallet.PayWalletService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.Validator;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.*;

/**
 * 支付渠道 Service 实现类
 *
 * @author aquan
 */
@Service
@Slf4j
@Validated
public class PayChannelServiceImpl implements PayChannelService {

    @Getter // 为了方便测试，这里提供 getter 方法
    @Setter
    private volatile List<PayChannelDO> channelCache;

    @Resource
    private PayClientFactory payClientFactory;

    @Resource
    private PayChannelMapper channelMapper;

    @Resource
    private Validator validator;
    @Resource
    private PayWalletService payWalletService;

    /**
     * 初始化 {@link #payClientFactory} 缓存
     */
    @PostConstruct
    public void initLocalCache() {
        // 注意：忽略自动多租户，因为要全局初始化缓存
        TenantUtils.executeIgnore(() -> {
            // 第一步：查询数据
            List<PayChannelDO> channels = Collections.emptyList();
            try {
                channels = channelMapper.selectList();
            } catch (Throwable ex) {
                if (!ex.getMessage().contains("doesn't exist")) {
                    throw ex;
                }
                log.error("[支付模块 yudao-module-pay - 表结构未导入][参考 https://doc.iocoder.cn/pay/build/ 开启]");
            }
            log.info("[initLocalCache][缓存支付渠道，数量为:{}]", channels.size());
            // 第二步：构建缓存：创建或更新支付 Client
            channels.forEach(payChannel ->{
                AbstractPayClient<PayClientConfig> payClient = createPayClient(payChannel.getId(), payChannel.getCode(), payChannel.getConfig());
                payClientFactory.addOrUpdatePayClient(payChannel.getId(), payClient);

            });
            this.channelCache = channels;
        });
    }
    @SuppressWarnings("unchecked")
    private <Config extends PayClientConfig> AbstractPayClient<Config> createPayClient(
            Long channelId, String channelCode, Config config) {
        PayChannelEnum channelEnum = PayChannelEnum.getByCode(channelCode);
        Assert.notNull(channelEnum, String.format("支付渠道(%s) 为空", channelEnum));
        // 创建客户端
        switch (channelEnum) {
            // 微信支付
            case WX_PUB:
                return (AbstractPayClient<Config>) new WxPubPayClient(channelId, (WxPayClientConfig) config);
            case WX_LITE:
                return (AbstractPayClient<Config>) new WxLitePayClient(channelId, (WxPayClientConfig) config);
            case WX_APP:
                return (AbstractPayClient<Config>) new WxAppPayClient(channelId, (WxPayClientConfig) config);
            case WX_BAR:
                return (AbstractPayClient<Config>) new WxBarPayClient(channelId, (WxPayClientConfig) config);
            case WX_NATIVE:
                return (AbstractPayClient<Config>) new WxNativePayClient(channelId, (WxPayClientConfig) config);
            // 支付宝支付
            case ALIPAY_WAP:
                return (AbstractPayClient<Config>) new AlipayWapPayClient(channelId, (AlipayPayClientConfig) config);
            case ALIPAY_QR:
                return (AbstractPayClient<Config>) new AlipayQrPayClient(channelId, (AlipayPayClientConfig) config);
            case ALIPAY_APP:
                return (AbstractPayClient<Config>) new AlipayAppPayClient(channelId, (AlipayPayClientConfig) config);
            case ALIPAY_PC:
                return (AbstractPayClient<Config>) new AlipayPcPayClient(channelId, (AlipayPayClientConfig) config);
            case ALIPAY_BAR:
                return (AbstractPayClient<Config>) new AlipayBarPayClient(channelId, (AlipayPayClientConfig) config);
            // 其它支付
            case MOCK:
                return (AbstractPayClient<Config>) new MockPayClient(channelId, (MockPayClientConfig) config);
            case WALLET:
                return (AbstractPayClient<Config>) new WalletPayClient(
                        channelId, channelCode, (NonePayClientConfig) config, payWalletService);
        }
        // 创建失败，错误日志 + 抛出异常
        log.error("[createPayClient][配置({}) 找不到合适的客户端实现]", config);
        throw new IllegalArgumentException(String.format("配置(%s) 找不到合适的客户端实现", config));
    }

    /**
     * 通过定时任务轮询，刷新缓存
     *
     * 目的：多节点部署时，通过轮询”通知“所有节点，进行刷新
     */
    @Scheduled(initialDelay = 60, fixedRate = 60, timeUnit = TimeUnit.SECONDS)
    public void refreshLocalCache() {
        // 注意：忽略自动多租户，因为要全局初始化缓存
        TenantUtils.executeIgnore(() -> {
            // 情况一：如果缓存里没有数据，则直接刷新缓存
            if (CollUtil.isEmpty(channelCache)) {
                initLocalCache();
                return;
            }

            // 情况二，如果缓存里数据，则通过 updateTime 判断是否有数据变更，有变更则刷新缓存
            LocalDateTime maxTime = CollectionUtils.getMaxValue(channelCache, PayChannelDO::getUpdateTime);
            if (channelMapper.selectCountByUpdateTimeGt(maxTime) > 0) {
                initLocalCache();
            }
        });
    }

    @Override
    public Long createChannel(PayChannelCreateReqVO reqVO) {
        // 断言是否有重复的
        PayChannelDO dbChannel = getChannelByAppIdAndCode(reqVO.getAppId(), reqVO.getCode());
        if (dbChannel != null) {
            throw exception(CHANNEL_EXIST_SAME_CHANNEL_ERROR);
        }

        // 新增渠道
        PayChannelDO channel = PayChannelConvert.INSTANCE.convert(reqVO)
                .setConfig(parseConfig(reqVO.getCode(), reqVO.getConfig()));
        channelMapper.insert(channel);

        // 刷新缓存
        initLocalCache();
        return channel.getId();
    }

    @Override
    public void updateChannel(PayChannelUpdateReqVO updateReqVO) {
        // 校验存在
        PayChannelDO dbChannel = validateChannelExists(updateReqVO.getId());

        // 更新
        PayChannelDO channel = PayChannelConvert.INSTANCE.convert(updateReqVO)
                .setConfig(parseConfig(dbChannel.getCode(), updateReqVO.getConfig()));
        channelMapper.updateById(channel);

        // 刷新缓存
        initLocalCache();
    }

    /**
     * 解析并校验配置
     *
     * @param code      渠道编码
     * @param configStr 配置
     * @return 支付配置
     */
    private PayClientConfig parseConfig(String code, String configStr) {
        // 解析配置
        Class<? extends PayClientConfig> payClass = PayChannelEnum.getByCode(code).getConfigClass();
        if (ObjectUtil.isNull(payClass)) {
            throw exception(CHANNEL_NOT_FOUND);
        }
        PayClientConfig config = JsonUtils.parseObject2(configStr, payClass);
        Assert.notNull(config);

        // 验证参数
        config.validate(validator);
        return config;
    }

    @Override
    public void deleteChannel(Long id) {
        // 校验存在
        validateChannelExists(id);

        // 删除
        channelMapper.deleteById(id);

        // 刷新缓存
        initLocalCache();
    }

    private PayChannelDO validateChannelExists(Long id) {
        PayChannelDO channel = channelMapper.selectById(id);
        if (channel == null) {
            throw exception(CHANNEL_NOT_FOUND);
        }
        return channel;
    }

    @Override
    public PayChannelDO getChannel(Long id) {
        return channelMapper.selectById(id);
    }

    @Override
    public List<PayChannelDO> getChannelListByAppIds(Collection<Long> appIds) {
        return channelMapper.selectListByAppIds(appIds);
    }

    @Override
    public PayChannelDO getChannelByAppIdAndCode(Long appId, String code) {
        return channelMapper.selectByAppIdAndCode(appId, code);
    }

    @Override
    public PayChannelDO validPayChannel(Long id) {
        PayChannelDO channel = channelMapper.selectById(id);
        validPayChannel(channel);
        return channel;
    }

    @Override
    public PayChannelDO validPayChannel(Long appId, String code) {
        PayChannelDO channel = channelMapper.selectByAppIdAndCode(appId, code);
        validPayChannel(channel);
        return channel;
    }

    private void validPayChannel(PayChannelDO channel) {
        if (channel == null) {
            throw exception(CHANNEL_NOT_FOUND);
        }
        if (CommonStatusEnum.DISABLE.getStatus().equals(channel.getStatus())) {
            throw exception(CHANNEL_IS_DISABLE);
        }
    }

    @Override
    public List<PayChannelDO> getEnableChannelList(Long appId) {
        return channelMapper.selectListByAppId(appId, CommonStatusEnum.ENABLE.getStatus());
    }

}
