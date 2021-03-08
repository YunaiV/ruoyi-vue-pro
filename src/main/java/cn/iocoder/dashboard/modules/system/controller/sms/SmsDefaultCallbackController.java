package cn.iocoder.dashboard.modules.system.controller.sms;

import cn.iocoder.dashboard.modules.system.service.sms.SysSmsService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;

/**
 * 短信默认回调接口
 *
 * @author zzf
 * @date 2021/3/5 8:59
 */
@RestController("/sms/callback")
public class SmsDefaultCallbackController {

    @Resource
    private SysSmsService smsService;

    @RequestMapping("/sms-send")
    public Object sendSmsCallback(ServletRequest request){
        return smsService.smsSendCallbackHandle(request);
    }

}
