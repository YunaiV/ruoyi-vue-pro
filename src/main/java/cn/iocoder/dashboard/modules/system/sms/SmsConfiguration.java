package cn.iocoder.dashboard.modules.system.sms;

import cn.iocoder.dashboard.framework.sms.SmsClient;
import cn.iocoder.dashboard.framework.sms.SmsClientAdapter;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.SmsChannelAllVO;
import cn.iocoder.dashboard.modules.system.service.sms.SmsChannelService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

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
    public SmsClientAdapter smsClientWrapper() {
        List<SmsChannelAllVO> smsChannelAllVOList = channelService.listChannelAllEnabledInfo();
        Map<Long, SmsClient<?>> channelId2SmsClientMap = SmsSenderUtils.init(smsChannelAllVOList);
        return new SmsClientAdapter(channelId2SmsClientMap);
    }

}
