package cn.iocoder.dashboard.modules.system.controller.sms;

import cn.hutool.core.util.URLUtil;
import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.framework.sms.core.enums.SmsChannelEnum;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api(tags = "短信回调")
@RestController
@RequestMapping("/system/sms/callback")
public class SmsCallbackController {

    @Resource
    private SysSmsService smsService;

    @PostMapping("/sms/yunpian")
    @ApiOperation(value = "云片短信的回调", notes = "参见 https://www.yunpian.com/official/document/sms/zh_cn/domestic_push_report 文档")
    @ApiImplicitParam(name = "sms_status", value = "发送状态", required = true, example = "[{具体内容}]", dataTypeClass = Long.class)
    public CommonResult<Boolean> receiveYunpianSmsStatus(@RequestParam("sms_status") String smsStatus) throws Throwable {
        String text = URLUtil.decode(smsStatus); // decode 解码参数，因为它被 encode
        smsService.receiveSmsStatus(SmsChannelEnum.YUN_PIAN.getCode(), text);
        return CommonResult.success(true);
    }

}
