package cn.iocoder.yudao.coreservice.modules.pay.service.merchant;

import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayChannelDO;
import cn.iocoder.yudao.framework.common.exception.ServiceException;

/**
 * 支付渠道 Core Service 接口
 *
 * @author 芋道源码
 */
public interface PayChannelCoreService {

    /**
     * 支付渠道的合法性
     *
     * 如果不合法，抛出 {@link ServiceException} 业务异常
     *
     * @param appId 应用编号
     * @param code 支付渠道
     * @return 渠道信息
     */
    PayChannelDO validPayChannel(Long appId, String code);

}
