package cn.iocoder.yudao.adminserver.modules.pay.service.channel.impl;

import cn.iocoder.yudao.adminserver.modules.pay.controller.channel.vo.PayChannelCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.pay.controller.channel.vo.PayChannelExportReqVO;
import cn.iocoder.yudao.adminserver.modules.pay.controller.channel.vo.PayChannelPageReqVO;
import cn.iocoder.yudao.adminserver.modules.pay.controller.channel.vo.PayChannelUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.pay.convert.channel.PayChannelConvert;
import cn.iocoder.yudao.adminserver.modules.pay.dal.mysql.channel.PayChannelMapper;
import cn.iocoder.yudao.adminserver.modules.pay.service.channel.PayChannelService;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayChannelDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.pay.core.client.impl.alipay.AlipayPayClientConfig;
import cn.iocoder.yudao.framework.pay.core.client.impl.wx.WXPayClientConfig;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelEnum;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.coreservice.modules.pay.enums.PayErrorCodeCoreConstants.*;
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

    @Override
    public Long createChannel(PayChannelCreateReqVO reqVO) {
        // TODO @aquan：感觉获得那一条比较合适。因为是有唯一性的。注释有错别字哈。
        // 判断是否有重复的有责无法新增
        Integer channelCount = this.getChannelCountByConditions(reqVO.getMerchantId(), reqVO.getAppId(), reqVO.getCode());
        if (channelCount > 0) {
            throw exception(CHANNEL_EXIST_SAME_CHANNEL_ERROR);
        }

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
     * 检测微信秘钥参数
     *
     * @param config 信秘钥参数
     */
    private void wechatParamCheck(WXPayClientConfig config) {
        // 针对于 V2 或者 V3 版本的参数校验
        if (WXPayClientConfig.API_VERSION_V2.equals(config.getApiVersion())) {
            Assert.notNull(config.getMchKey(), CHANNEL_WECHAT_VERSION_2_MCH_KEY_IS_NULL.getMsg());
        }
        if (WXPayClientConfig.API_VERSION_V3.equals(config.getApiVersion())) {
            Assert.notNull(config.getPrivateKeyContent(), CHANNEL_WECHAT_VERSION_3_PRIVATE_KEY_IS_NULL.getMsg());
            Assert.notNull(config.getPrivateCertContent(), CHANNEL_WECHAT_VERSION_3_CERT_KEY_IS_NULL.getMsg());
        }
    }



    /**
     * 设置渠道配置以及参数校验
     *
     * @param channel   渠道
     * @param configStr 配置
     */
    private void settingConfigAndCheckParam(PayChannelDO channel, String configStr) {
        // 得到这个渠道是微信的还是支付宝的
        String channelType = PayChannelEnum.verifyWechatOrAliPay(channel.getCode());
        Assert.notNull(channelType, CHANNEL_NOT_EXISTS.getMsg());

        // 进行验证
        // TODO @阿全：Spring 可以注入 Validator 哈
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();

        // 微信的验证
        // TODO @aquan：这么实现，可扩性不好。@AssertTrue 注解。
        if (PayChannelEnum.WECHAT.equals(channelType)) {

            WXPayClientConfig config = JSON.parseObject(configStr, WXPayClientConfig.class);
            // 判断是V2 版本还是 V3 版本
            Class clazz = config.getApiVersion().equals(WXPayClientConfig.API_VERSION_V2)
                    ? WXPayClientConfig.V2.class : WXPayClientConfig.V3.class;
            // 手动调用validate进行验证
            Set<ConstraintViolation<WXPayClientConfig>> validate = validator.validate(config,clazz);

            // 断言没有异常
            Assert.isTrue(validate.isEmpty(), validate.stream().map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(",")));

            channel.setConfig(config);
        }

        // 支付宝验证
        if (PayChannelEnum.ALIPAY.equals(channelType)) {

            AlipayPayClientConfig config = JSON.parseObject(configStr, AlipayPayClientConfig.class);

            // 判断是V2 版本还是 V3 版本
            Class clazz = config.getMode().equals(AlipayPayClientConfig.MODE_PUBLIC_KEY)
                    ? AlipayPayClientConfig.ModePublicKey.class : AlipayPayClientConfig.ModeCertificate.class;
            // 手动调用validate进行验证
            Set<ConstraintViolation<AlipayPayClientConfig>> validate = validator.validate(config,clazz);

            // 断言没有异常
            Assert.isTrue(validate.isEmpty(), validate.stream().map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(",")));
            channel.setConfig(config);
        }

    }
}
