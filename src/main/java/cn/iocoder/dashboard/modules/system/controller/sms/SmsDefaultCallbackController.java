package cn.iocoder.dashboard.modules.system.controller.sms;

import cn.iocoder.dashboard.modules.system.service.sms.SysSmsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
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
@Api(tags = "短信回调api")
@RestController
@RequestMapping("/sms/callback")
public class SmsDefaultCallbackController {

    @Resource
    private SysSmsService smsService;


    @ApiOperation(value = "短信发送回调接口")
    @PostMapping("/sms-send")
    public Object sendSmsCallback(ServletRequest request) {
        return smsService.smsSendCallbackHandle(request);
    }

/*
    @Resource
    private SmsSendStreamProducer smsSendStreamProducer;

    @ApiOperation("redis stream测试")
    @GetMapping("/test/redis/stream")
    public void test() {
        SmsBody smsBody = new SmsBody();
        smsBody.setSmsLogId(1L);
        smsBody.setTemplateCode("sdf");
        smsBody.setTemplateContent("sdf");
        smsSendStreamProducer.sendSmsSendMessage(smsBody, "18216466755");
    }*/

}
