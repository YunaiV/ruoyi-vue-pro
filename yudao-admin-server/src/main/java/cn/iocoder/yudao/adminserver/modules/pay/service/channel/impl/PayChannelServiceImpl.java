package cn.iocoder.yudao.adminserver.modules.pay.service.channel.impl;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.adminserver.modules.pay.controller.channel.vo.PayChannelCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.pay.controller.channel.vo.PayChannelExportReqVO;
import cn.iocoder.yudao.adminserver.modules.pay.controller.channel.vo.PayChannelPageReqVO;
import cn.iocoder.yudao.adminserver.modules.pay.controller.channel.vo.PayChannelUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.pay.convert.channel.PayChannelConvert;
import cn.iocoder.yudao.adminserver.modules.pay.dal.mysql.channel.PayChannelMapper;
import cn.iocoder.yudao.adminserver.modules.pay.service.channel.PayChannelService;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayChannelDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.pay.core.client.PayClientConfig;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Validator;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.coreservice.modules.pay.enums.PayErrorCodeCoreConstants.CHANNEL_EXIST_SAME_CHANNEL_ERROR;
import static cn.iocoder.yudao.coreservice.modules.pay.enums.PayErrorCodeCoreConstants.CHANNEL_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

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
    private PayChannelMapper channelMapper;

    @Resource
    private Validator validator;

    @Override
    public Long createChannel(PayChannelCreateReqVO reqVO) {
        // 断言是否有重复的
        PayChannelDO channelDO = this.getChannelByConditions(reqVO.getMerchantId(), reqVO.getAppId(), reqVO.getCode());
        // TODO @aquan：这里会抛出系统异常，不会抛出 ServiceException
        Assert.isNull(channelDO, CHANNEL_EXIST_SAME_CHANNEL_ERROR.getMsg());

        // 新增渠道
        PayChannelDO channel = PayChannelConvert.INSTANCE.convert(reqVO);
        settingConfigAndCheckParam(channel, reqVO.getConfig());
        channelMapper.insert(channel);
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
    }

    @Override
    public void deleteChannel(Long id) {
        // 校验存在
        this.validateChannelExists(id);
        // 删除
        channelMapper.deleteById(id);
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
     * 根据条件获取通道数量
     *
     * @param merchantId 商户编号
     * @param appid      应用编号
     * @param code       通道编码
     * @return 数量
     */
    @Override
    public Integer getChannelCountByConditions(Long merchantId, Long appid, String code) {
        return this.channelMapper.getChannelCountByConditions(merchantId, appid, code);
    }

    /**
     * 根据条件获取通道
     *
     * @param merchantId 商户编号
     * @param appid      应用编号
     * @param code       通道编码
     * @return 数量
     */
    @Override
    public PayChannelDO getChannelByConditions(Long merchantId, Long appid, String code) {
        return this.channelMapper.getChannelByConditions(merchantId, appid, code);
    }

    /**
     * 设置渠道配置以及参数校验
     *
     * @param channel   渠道
     * @param configStr 配置
     */
    private void settingConfigAndCheckParam(PayChannelDO channel, String configStr) {
        // 得到这个渠道是微信的还是支付宝的
        Class<? extends PayClientConfig> payClass = PayChannelEnum.findByCodeGetClass(channel.getCode());
        Assert.notNull(payClass, CHANNEL_NOT_EXISTS.getMsg());
        PayClientConfig config = JSONUtil.toBean(configStr, payClass);
        // 验证参数
        config.verifyParam(validator);
        channel.setConfig(config);
    }

}
