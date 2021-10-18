package cn.iocoder.yudao.coreservice.modules.pay.service.merchant.impl;

import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayChannelDO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.mysql.merchant.PayChannelCoreMapper;
import cn.iocoder.yudao.coreservice.modules.pay.enums.PayErrorCodeConstants;
import cn.iocoder.yudao.coreservice.modules.pay.service.merchant.PayChannelCoreService;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.coreservice.modules.pay.enums.PayErrorCodeConstants.*;
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

    @Resource
    private PayChannelCoreMapper payChannelCoreMapper;

    @Override
    public PayChannelDO validPayChannel(Long appId, String code) {
        PayChannelDO channel = payChannelCoreMapper.selectByAppIdAndCode(appId, code);
        if (channel == null) {
            throw exception(PAY_CHANNEL_NOT_FOUND);
        }
        if (CommonStatusEnum.DISABLE.getStatus().equals(channel.getStatus())) {
            throw exception(PayErrorCodeConstants.PAY_CHANNEL_IS_DISABLE);
        }
        return channel;
    }

}
