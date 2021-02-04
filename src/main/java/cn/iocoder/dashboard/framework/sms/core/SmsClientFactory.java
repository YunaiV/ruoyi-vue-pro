package cn.iocoder.dashboard.framework.sms.core;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.dashboard.common.enums.SmsChannelEnum;
import cn.iocoder.dashboard.common.exception.ServiceException;
import cn.iocoder.dashboard.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.dashboard.framework.sms.client.AbstractSmsClient;
import cn.iocoder.dashboard.framework.sms.client.AliyunSmsClient;
import cn.iocoder.dashboard.framework.sms.core.property.SmsChannelProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static cn.iocoder.dashboard.modules.system.enums.SysErrorCodeConstants.*;

/**
 * 短信客户端工厂
 *
 * @author zzf
 * @date 2021/1/28 14:01
 */
@Component
public class SmsClientFactory {

    private final Map<Long, AbstractSmsClient<?>> smsSenderMap = new ConcurrentHashMap<>(8);

    /**
     * 创建短信客户端
     *
     * @param propertyVO 参数对象
     * @return 客户端id(默认channelId)
     */
    public Long createClient(SmsChannelProperty propertyVO) {
        if (StrUtil.isBlank(propertyVO.getCode())) {
            throw ServiceExceptionUtil.exception(PARAM_VALUE_IS_NULL, "短信渠道编码");
        }
        if (ObjectUtil.isNull(propertyVO.getId())) {
            throw ServiceExceptionUtil.exception(PARAM_VALUE_IS_NULL, "短信渠道ID");
        }

        AbstractSmsClient<?> sender = createClient(SmsChannelEnum.getByCode(propertyVO.getCode()), propertyVO);
        smsSenderMap.put(propertyVO.getId(), sender);
        return propertyVO.getId();
    }

    private AbstractSmsClient<?> createClient(SmsChannelEnum channelEnum, SmsChannelProperty channelVO) {
        if (channelEnum == null) {
            throw new ServiceException(INVALID_CHANNEL_CODE);
        }
        switch (channelEnum) {
            case ALI:
                return new AliyunSmsClient(channelVO);
            // TODO fill more channel
            default:
                break;
        }
        throw new ServiceException(SMS_SENDER_NOT_FOUND);
    }

    /**
     * 获取短信客户端
     *
     * @param channelId 渠道id
     * @return 短信id
     */
    public AbstractSmsClient<?> getClient(Long channelId) {
        return smsSenderMap.get(channelId);
    }
}
