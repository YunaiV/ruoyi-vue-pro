package cn.iocoder.dashboard.modules.system.sms;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.dashboard.common.enums.SmsChannelEnum;
import cn.iocoder.dashboard.common.exception.ServiceException;
import cn.iocoder.dashboard.framework.sms.SmsBody;
import cn.iocoder.dashboard.framework.sms.SmsClient;
import cn.iocoder.dashboard.framework.sms.SmsClientAdapter;
import cn.iocoder.dashboard.framework.sms.SmsResult;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.SmsChannelAllVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.SmsTemplateVO;
import cn.iocoder.dashboard.modules.system.sms.client.AliSmsClient;
import cn.iocoder.dashboard.modules.system.sms.proxy.SmsClientLogProxy;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static cn.iocoder.dashboard.modules.system.enums.SysErrorCodeConstants.*;

/**
 * 短信发送者工厂
 *
 * @author zzf
 * @date 2021/1/25 16:18
 */
public class SmsSenderUtils {

    /**
     * 短信渠道id:短信客户端map
     * key: channelId
     * val: SmsClient
     */
    private static final Map<Long, SmsClient<?>> smsSenderMap = new ConcurrentHashMap<>(8);

    /**
     * 短信模板code: 短信渠道id map
     * key: templateCode
     * val: channelId
     */
    private static final Map<String, Long> templateCode2ChannelIdMap = new HashMap<>();

    /**
     * 将短信渠道信息初始化成短信客户端
     *
     * @param smsChannelAllVOList 短信渠道信息
     * @return 短信渠道id:短信客户端map
     */
    public synchronized static Map<Long, SmsClient<?>> init(List<SmsChannelAllVO> smsChannelAllVOList) {
        if (ObjectUtil.isEmpty(smsChannelAllVOList)) {
            throw new ServiceException(SMS_CHANNEL_NOT_FOUND);
        }
        addSender(smsChannelAllVOList);
        return smsSenderMap;
    }

    /**
     * 重置短信客户端信息
     *
     * @param smsClientAdapter    短信客户端适配器
     * @param smsChannelAllVOList 短信渠道信息集合
     */
    public synchronized static void flush(SmsClientAdapter smsClientAdapter, List<SmsChannelAllVO> smsChannelAllVOList) {
        smsSenderMap.clear();
        smsClientAdapter.flushClient(init(smsChannelAllVOList));
    }

    /**
     * 发送短信
     *
     * @param smsClientAdapter 短信客户端适配器
     * @param smsBody          短信内容
     * @param targetPhones     对象手机集合
     * @return 短信发送结果
     */
    public static SmsResult<?> send(SmsClientAdapter smsClientAdapter, SmsBody smsBody, Collection<String> targetPhones) {
        Long channelId = templateCode2ChannelIdMap.get(smsBody.getCode());
        if (channelId == null) {
            throw new ServiceException(SMS_SENDER_NOT_FOUND);
        }
        return smsClientAdapter.send(channelId, smsBody, targetPhones);
    }

    /**
     * 发送短信
     *
     * @param smsClientAdapter 短信客户端适配器
     * @param smsBody          短信内容
     * @param targetPhone      对象手机
     * @return 短信发送结果
     */
    public static SmsResult<?> send(SmsClientAdapter smsClientAdapter, SmsBody smsBody, String targetPhone) {
        Long channelId = templateCode2ChannelIdMap.get(smsBody.getCode());
        if (channelId == null) {
            throw new ServiceException(SMS_SENDER_NOT_FOUND);
        }
        return smsClientAdapter.send(channelId, smsBody, Collections.singletonList(targetPhone));
    }

    /**
     * 发送短信
     *
     * @param smsClientAdapter 短信客户端适配器
     * @param smsBody          短信内容
     * @param targetPhones     对象手机数组
     * @return 短信发送结果
     */
    public static SmsResult<?> send(SmsClientAdapter smsClientAdapter, SmsBody smsBody, String... targetPhones) {
        Long channelId = templateCode2ChannelIdMap.get(smsBody.getCode());
        if (channelId == null) {
            throw new ServiceException(SMS_SENDER_NOT_FOUND);
        }
        return smsClientAdapter.send(channelId, smsBody, Arrays.asList(targetPhones));
    }


    private static void addSender(List<SmsChannelAllVO> smsChannelAllVOList) {
        smsChannelAllVOList.forEach(channelAllVO -> addSender(SmsChannelEnum.getByCode(channelAllVO.getCode()), channelAllVO));
    }

    private static void addSender(SmsChannelEnum channelEnum, SmsChannelAllVO channelAllVO) {
        if (channelEnum == null) {
            throw new ServiceException(INVALID_CHANNEL_CODE);
        }
        List<SmsTemplateVO> templateList = channelAllVO.getTemplateList();
        if (ObjectUtil.isEmpty(templateList)) {
            throw new ServiceException(SMS_TEMPLATE_NOT_FOUND);
        }
        SmsClient<?> aliSmsClient = getSender(channelEnum, channelAllVO);
        templateList.forEach(smsTemplateVO -> templateCode2ChannelIdMap.put(smsTemplateVO.getCode(), channelAllVO.getId()));
        smsSenderMap.put(channelAllVO.getId(), aliSmsClient);
    }

    private static SmsClient<?> getSender(SmsChannelEnum channelEnum, SmsChannelAllVO channelAllVO) {
        switch (channelEnum) {
            case ALI:
                return new SmsClientLogProxy<>(new AliSmsClient(channelAllVO));
            // TODO fill more channel
            default:
                break;
        }
        throw new ServiceException(SMS_SENDER_NOT_FOUND);
    }
}
