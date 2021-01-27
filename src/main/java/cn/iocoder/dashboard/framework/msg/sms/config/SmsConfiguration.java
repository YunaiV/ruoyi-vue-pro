package cn.iocoder.dashboard.framework.msg.sms.config;

import cn.iocoder.dashboard.framework.msg.sms.factory.DefaultSmsSenderFactory;
import cn.iocoder.dashboard.framework.msg.sms.intercepter.AbstractSmsIntercepterChain;
import cn.iocoder.dashboard.framework.msg.sms.intercepter.DefaultSmsIntercepterChain;
import cn.iocoder.dashboard.framework.msg.sms.intercepter.SmsLogIntercepter;
import cn.iocoder.dashboard.modules.msg.controller.sms.vo.SmsChannelAllVO;
import cn.iocoder.dashboard.modules.msg.service.sms.SmsChannelService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.List;

/**
 * 短信服务配置
 *
 * @author guer
 */
@Configuration
@ConditionalOnProperty("sms.enabled")
public class SmsConfiguration {

    @Resource
    private SmsChannelService channelService;


    @Bean
    public AbstractSmsIntercepterChain smsIntercepterChain() {
        DefaultSmsIntercepterChain intercepterChain = new DefaultSmsIntercepterChain();
        //添加拦截器
        intercepterChain.addSmsIntercepter(new SmsLogIntercepter());
        return intercepterChain;
    }

    @Bean
    public DefaultSmsSenderFactory smsSenderFactory(AbstractSmsIntercepterChain intercepterChain) {
        DefaultSmsSenderFactory defaultSmsSenderFactory = new DefaultSmsSenderFactory();
        List<SmsChannelAllVO> smsChannelAllVOList = channelService.listChannelAllEnabledInfo();
        //初始化渠道、模板信息
        defaultSmsSenderFactory.init(smsChannelAllVOList);
        //注入拦截器链
        defaultSmsSenderFactory.setIntercepterChain(intercepterChain);
        return defaultSmsSenderFactory;
    }

}
