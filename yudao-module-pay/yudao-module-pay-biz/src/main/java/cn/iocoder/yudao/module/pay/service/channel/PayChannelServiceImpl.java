package cn.iocoder.yudao.module.pay.service.channel;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.pay.core.client.PayClientConfig;
import cn.iocoder.yudao.framework.pay.core.client.PayClientFactory;
import cn.iocoder.yudao.framework.pay.core.enums.channel.PayChannelEnum;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.pay.controller.admin.channel.vo.PayChannelCreateReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.channel.vo.PayChannelPageReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.channel.vo.PayChannelUpdateReqVO;
import cn.iocoder.yudao.module.pay.convert.channel.PayChannelConvert;
import cn.iocoder.yudao.module.pay.dal.dataobject.channel.PayChannelDO;
import cn.iocoder.yudao.module.pay.dal.mysql.channel.PayChannelMapper;
import cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.Validator;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.CHANNEL_EXIST_SAME_CHANNEL_ERROR;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.CHANNEL_NOT_EXISTS;

/**
 * 支付渠道 Service 实现类
 *
 * @author aquan
 */
@Service
@Slf4j
@Validated
public class PayChannelServiceImpl implements PayChannelService {

    @Resource
    private PayClientFactory payClientFactory;

    @Resource
    private PayChannelMapper channelMapper;

    @Resource
    private Validator validator;

    /**
     * 初始化 {@link #payClientFactory} 缓存
     */
    @Override
    @PostConstruct
    public void initLocalCache() {
        // 注意：忽略自动多租户，因为要全局初始化缓存
        TenantUtils.executeIgnore(() -> {
            // 第一步：查询数据
            List<PayChannelDO> channels = channelMapper.selectList();
            log.info("[initLocalCache][缓存支付渠道，数量为:{}]", channels.size());

            // 第二步：构建缓存：创建或更新支付 Client
            channels.forEach(payChannel -> payClientFactory.createOrUpdatePayClient(payChannel.getId(),
                    payChannel.getCode(), payChannel.getConfig()));
        });
    }

    @Override
    public Long createChannel(PayChannelCreateReqVO reqVO) {
        // 断言是否有重复的
        PayChannelDO channelDO = this.getChannelByConditions(reqVO.getAppId(), reqVO.getCode());
        if (ObjectUtil.isNotNull(channelDO)) {
            throw exception(CHANNEL_EXIST_SAME_CHANNEL_ERROR);
        }

        // 新增渠道
        PayChannelDO channel = PayChannelConvert.INSTANCE.convert(reqVO);
        settingConfigAndCheckParam(channel, reqVO.getConfig());
        channelMapper.insert(channel);
        // TODO 芋艿：缺少刷新本地缓存的机制
        return channel.getId();
    }

    @Override
    public void updateChannel(PayChannelUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateChannelExists(updateReqVO.getId());
        // 更新
        PayChannelDO channel = PayChannelConvert.INSTANCE.convert(updateReqVO);
        settingConfigAndCheckParam(channel, updateReqVO.getConfig());
        channelMapper.updateById(channel);
        // TODO 芋艿：缺少刷新本地缓存的机制
    }

    @Override
    public void deleteChannel(Long id) {
        // 校验存在
        this.validateChannelExists(id);
        // 删除
        channelMapper.deleteById(id);
        // TODO 芋艿：缺少刷新本地缓存的机制
    }

    private void validateChannelExists(Long id) {
        if (channelMapper.selectById(id) == null) {
            throw exception(CHANNEL_NOT_EXISTS);
        }
    }

    @Override
    public PayChannelDO getChannel(Long id) {
        return channelMapper.selectById(id);
    }

    @Override
    public PageResult<PayChannelDO> getChannelPage(PayChannelPageReqVO pageReqVO) {
        return channelMapper.selectPage(pageReqVO);
    }

    @Override
    public List<PayChannelDO> getChannelListByAppIds(Collection<Long> appIds) {
        return channelMapper.getChannelListByAppIds(appIds);
    }

    @Override
    public PayChannelDO getChannelByConditions(Long appid, String code) {
        return this.channelMapper.selectOne(appid, code);
    }

    /**
     * 设置渠道配置以及参数校验
     *
     * @param channel   渠道
     * @param configStr 配置
     */
    private void settingConfigAndCheckParam(PayChannelDO channel, String configStr) {
        // 得到这个渠道是微信的还是支付宝的
        Class<? extends PayClientConfig> payClass = PayChannelEnum.getByCode(channel.getCode()).getConfigClass();
        if (ObjectUtil.isNull(payClass)) {
            throw exception(CHANNEL_NOT_EXISTS);
        }
        // TODO @芋艿：不要使用 hutool 的 json 工具，用项目的
        PayClientConfig config = JSONUtil.toBean(configStr, payClass);

        // 验证参数
        config.validate(validator);
        channel.setConfig(config);
    }

    @Override
    public PayChannelDO validPayChannel(Long id) {
        PayChannelDO channel = channelMapper.selectById(id);
        this.validPayChannel(channel);
        return channel;
    }

    @Override
    public PayChannelDO validPayChannel(Long appId, String code) {
        PayChannelDO channel = channelMapper.selectByAppIdAndCode(appId, code);
        validPayChannel(channel);
        return channel;
    }

    @Override
    public List<PayChannelDO> getEnableChannelList(Long appId) {
        return channelMapper.selectListByAppId(appId, CommonStatusEnum.ENABLE.getStatus());
    }

    private void validPayChannel(PayChannelDO channel) {
        if (channel == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.PAY_CHANNEL_NOT_FOUND);
        }
        if (CommonStatusEnum.DISABLE.getStatus().equals(channel.getStatus())) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.PAY_CHANNEL_IS_DISABLE);
        }
    }

}
