package cn.iocoder.dashboard.framework.sms.core;

import cn.iocoder.dashboard.common.exception.ServiceException;
import cn.iocoder.dashboard.framework.sms.client.AbstractSmsClient;
import cn.iocoder.dashboard.framework.sms.client.impl.ali.AliyunSmsClient;
import cn.iocoder.dashboard.framework.sms.client.impl.yunpian.YunpianSmsClient;
import cn.iocoder.dashboard.framework.sms.core.enums.SmsChannelEnum;
import cn.iocoder.dashboard.framework.sms.core.property.SmsChannelProperty;
import cn.iocoder.dashboard.framework.sms.core.property.SmsTemplateProperty;
import cn.iocoder.dashboard.util.json.JsonUtils;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequest;
import java.util.Collection;
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

    /**
     * channelId: client map
     * 保存 渠道id: 对应短信客户端 的map
     */
    private final Map<Long, AbstractSmsClient> smsSenderMap = new ConcurrentHashMap<>(8);

    /**
     * templateCode: TemplateProperty map
     * 保存 模板编码：模板信息 的map
     */
    private final Map<String, SmsTemplateProperty> templatePropertyMap = new ConcurrentHashMap<>(16);

    /**
     * 创建短信客户端
     *
     * @param propertyVO 参数对象
     * @return 客户端id(默认channelId)
     */
    public Long createClient(SmsChannelProperty propertyVO) {
        AbstractSmsClient sender = createClient(SmsChannelEnum.getByCode(propertyVO.getCode()), propertyVO);
        smsSenderMap.put(propertyVO.getId(), sender);
        return propertyVO.getId();
    }

    private AbstractSmsClient createClient(SmsChannelEnum channelEnum, SmsChannelProperty channelVO) {
        if (channelEnum == null) {
            throw new ServiceException(INVALID_CHANNEL_CODE);
        }
        switch (channelEnum) {
            case ALI:
                return new AliyunSmsClient(channelVO);
            case YUN_PIAN:
                return new YunpianSmsClient(channelVO);
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
    public AbstractSmsClient getClient(Long channelId) {
        return smsSenderMap.get(channelId);
    }


    /**
     * 添加或修改短信模板信息缓存
     */
    public void addOrUpdateTemplateCache(Collection<SmsTemplateProperty> templateProperties) {
        templateProperties.forEach(this::addOrUpdateTemplateCache);
    }


    /**
     * 添加或修改短信模板信息缓存
     */
    public void addOrUpdateTemplateCache(SmsTemplateProperty templateProperty) {
        templatePropertyMap.put(templateProperty.getCode(), templateProperty);
    }


    /**
     * 根据短信模板编码获取模板唯一标识
     *
     * @param templateCode 短信模板编码
     * @return 短信id
     */
    public String getTemplateApiIdByCode(String templateCode) {
        SmsTemplateProperty smsTemplateProperty = templatePropertyMap.get(templateCode);
        if (smsTemplateProperty == null) {
            throw new ServiceException(SMS_TEMPLATE_NOT_FOUND);
        }
        return smsTemplateProperty.getApiTemplateId();
    }


    /**
     * 从短信发送回调函数请求中获取用于唯一确定一条send_lod的apiId
     *
     * @param callbackRequest 短信发送回调函数请求
     * @return 第三方平台短信唯一标识
     */
    public SmsResultDetail getSmsResultDetailFromCallbackQuery(ServletRequest callbackRequest) {

        for (Long channelId : smsSenderMap.keySet()) {
            AbstractSmsClient smsClient = smsSenderMap.get(channelId);
            try {
                SmsResultDetail smsSendResult = smsClient.smsSendCallbackHandle(callbackRequest);
                if (smsSendResult != null) {
                    return smsSendResult;
                }
            } catch (Exception ignored) {
            }
        }
        throw new IllegalArgumentException("getSmsResultDetailFromCallbackQuery fail! don't match SmsClient by RequestParam: "
                + JsonUtils.toJsonString(callbackRequest.getParameterMap()));
    }


}
