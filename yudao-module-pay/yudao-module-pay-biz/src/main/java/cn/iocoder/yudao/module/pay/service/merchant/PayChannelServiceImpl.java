package cn.iocoder.yudao.module.pay.service.merchant;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.pay.core.client.PayClientConfig;
import cn.iocoder.yudao.framework.pay.core.client.PayClientFactory;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelEnum;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.channel.PayChannelCreateReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.channel.PayChannelExportReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.channel.PayChannelPageReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.channel.PayChannelUpdateReqVO;
import cn.iocoder.yudao.module.pay.convert.channel.PayChannelConvert;
import cn.iocoder.yudao.module.pay.dal.dataobject.merchant.PayChannelDO;
import cn.iocoder.yudao.module.pay.dal.mysql.merchant.PayChannelMapper;
import cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.Validator;
import java.time.LocalDateTime;
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

    /**
     * 缓存菜单的最大更新时间，用于后续的增量轮询，判断是否有更新
     */
    private volatile LocalDateTime maxUpdateTime;

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
        PayChannelDO channelDO = this.getChannelByConditions(reqVO.getMerchantId(), reqVO.getAppId(), reqVO.getCode());
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
    public List<PayChannelDO> getChannelList(Collection<Long> ids) {
        return channelMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<PayChannelDO> getChannelPage(PayChannelPageReqVO pageReqVO) {
        return channelMapper.selectPage(pageReqVO);
    }

    @Override
    public List<PayChannelDO> getChannelList(PayChannelExportReqVO exportReqVO) {
        return channelMapper.selectList(exportReqVO);
    }

    /**
     * 根据支付应用ID集合获得支付渠道列表
     *
     * @param appIds 应用编号集合
     * @return 支付渠道列表
     */
    @Override
    public List<PayChannelDO> getChannelListByAppIds(Collection<Long> appIds) {
        return channelMapper.getChannelListByAppIds(appIds);
    }


    /**
     * 根据条件获取渠道数量
     *
     * @param merchantId 商户编号
     * @param appid      应用编号
     * @param code       渠道编码
     * @return 数量
     */
    @Override
    public Integer getChannelCountByConditions(Long merchantId, Long appid, String code) {
        return this.channelMapper.selectCount(merchantId, appid, code);
    }

    /**
     * 根据条件获取渠道
     *
     * @param merchantId 商户编号
     * @param appid      应用编号
     * @param code       渠道编码
     * @return 数量
     */
    @Override
    public PayChannelDO getChannelByConditions(Long merchantId, Long appid, String code) {
        return this.channelMapper.selectOne(merchantId, appid, code);
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
        this.validPayChannel(channel);
        return channel;
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
