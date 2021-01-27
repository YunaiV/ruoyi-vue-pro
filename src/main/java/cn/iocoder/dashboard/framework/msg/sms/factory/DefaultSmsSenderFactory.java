package cn.iocoder.dashboard.framework.msg.sms.factory;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.dashboard.common.enums.SmsChannelEnum;
import cn.iocoder.dashboard.common.exception.ServiceException;
import cn.iocoder.dashboard.framework.msg.sms.SmsSender;
import cn.iocoder.dashboard.framework.msg.sms.impl.ali.AliSmsSender;
import cn.iocoder.dashboard.framework.msg.sms.intercepter.AbstractSmsIntercepterChain;
import cn.iocoder.dashboard.framework.msg.sms.proxy.DefaultSmsSenderProxy;
import cn.iocoder.dashboard.modules.msg.controller.sms.vo.SmsChannelAllVO;
import cn.iocoder.dashboard.modules.msg.controller.sms.vo.SmsTemplateVO;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static cn.iocoder.dashboard.modules.system.enums.SysErrorCodeConstants.*;

/**
 * 短信发送者工厂
 *
 * @author zzf
 * @date 2021/1/25 16:18
 */
public class DefaultSmsSenderFactory {

    /**
     * sender索引
     * key: {@link SmsTemplateVO#getBizCode()}
     * value: {@link SmsSender}
     */
    private final ConcurrentHashMap<String, SmsSender<?>> bizCode2SenderMap = new ConcurrentHashMap<>(8);

    /**
     * sender索引
     * key: {@link SmsTemplateVO#getCode()}
     * value: {@link SmsSender}
     */
    private final ConcurrentHashMap<String, SmsSender<?>> templateCode2SenderMap = new ConcurrentHashMap<>(8);


    @Setter
    private AbstractSmsIntercepterChain intercepterChain;

    /**
     * 读写锁
     */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();


    public void init(List<SmsChannelAllVO> smsChannelAllVOList) {
        if (ObjectUtil.isEmpty(smsChannelAllVOList)) {
            throw new ServiceException(SMS_CHANNEL_NOT_FOUND);
        }
        try {
            writeLock.lock();
            addSender(smsChannelAllVOList);
        } finally {
            writeLock.unlock();
        }
    }

    public SmsSender<?> getSenderByBizCode(String bizCode) {
        return getSmsSender(bizCode, bizCode2SenderMap);
    }

    public SmsSender<?> getSenderByTemplateCode(String templateCode) {
        return getSmsSender(templateCode, templateCode2SenderMap);
    }

    private SmsSender<?> getSmsSender(String templateCode, ConcurrentHashMap<String, SmsSender<?>> cacheMap) {
        try {
            readLock.lock();
            SmsSender<?> smsSender = cacheMap.get(templateCode);
            if (smsSender == null) {
                throw new ServiceException(SMS_SENDER_NOT_FOUND);
            }
            return smsSender;
        } finally {
            readLock.unlock();
        }
    }

    public void flush(List<SmsChannelAllVO> smsChannelAllVOList) {
        try {
            writeLock.lock();
            bizCode2SenderMap.clear();
            templateCode2SenderMap.clear();
            addSender(smsChannelAllVOList);
        } finally {
            writeLock.unlock();
        }
    }


    private void addSender(List<SmsChannelAllVO> smsChannelAllVOList) {
        smsChannelAllVOList.forEach(channelAllVO -> addSender(SmsChannelEnum.getByCode(channelAllVO.getCode()), channelAllVO));
    }

    private void addSender(SmsChannelEnum channelEnum, SmsChannelAllVO channelAllVO) {
        if (channelEnum == null) {
            throw new ServiceException(INVALID_CHANNEL_CODE);
        }
        List<SmsTemplateVO> templateList = channelAllVO.getTemplateList();
        if (ObjectUtil.isEmpty(templateList)) {
            throw new ServiceException(SMS_TEMPLATE_NOT_FOUND);
        }

        SmsSender<?> aliSmsSender = getSender(channelEnum, channelAllVO);


        templateList.forEach(smsTemplateVO -> {
            bizCode2SenderMap.put(smsTemplateVO.getBizCode(), aliSmsSender);
            templateCode2SenderMap.put(smsTemplateVO.getCode(), aliSmsSender);
        });
    }

    private SmsSender<?> getSender(SmsChannelEnum channelEnum, SmsChannelAllVO channelAllVO) {
        switch (channelEnum) {
            case ALI:
                return new DefaultSmsSenderProxy<>(new AliSmsSender(channelAllVO), intercepterChain);
            // TODO fill more channel
            default:
                break;
        }
        throw new ServiceException(SMS_SENDER_NOT_FOUND);
    }




}
