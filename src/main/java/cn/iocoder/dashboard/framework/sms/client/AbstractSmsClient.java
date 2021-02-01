package cn.iocoder.dashboard.framework.sms.client;

import cn.iocoder.dashboard.modules.system.controller.sms.vo.SmsChannelPropertyVO;

/**
 * 抽象短息客户端
 *
 * @author zzf
 * @date 2021/2/1 9:28
 */
public abstract class AbstractSmsClient<R> implements SmsClient<R> {

    /**
     * 短信渠道参数
     */
    protected final SmsChannelPropertyVO channelVO;

    /**
     * 构造阿里云短信发送处理
     *
     * @param channelVO 阿里云短信配置
     */
    public AbstractSmsClient(SmsChannelPropertyVO channelVO) {
        this.channelVO = channelVO;
    }


    public SmsChannelPropertyVO getProperty() {
        return channelVO;
    }

}
