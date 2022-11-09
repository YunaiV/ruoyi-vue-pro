package cn.iocoder.yudao.module.system.controller.admin.sms;

import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.iocoder.yudao.module.system.service.sms.SmsSendService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.framework.sms.core.enums.SmsChannelEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "管理后台 - 短信回调")
@RestController
@RequestMapping("/system/sms/callback")
public class SmsCallbackController {

    @Resource
    private SmsSendService smsSendService;

    @PostMapping("/yunpian")
    @PermitAll
    @ApiOperation(value = "云片短信的回调", notes = "参见 https://www.yunpian.com/official/document/sms/zh_cn/domestic_push_report 文档")
    @ApiImplicitParam(name = "sms_status", value = "发送状态", required = true, example = "[{具体内容}]", dataTypeClass = String.class)
    @OperateLog(enable = false)
    public String receiveYunpianSmsStatus(@RequestParam("sms_status") String smsStatus) throws Throwable {
        String text = URLUtil.decode(smsStatus); // decode 解码参数，因为它被 encode
        smsSendService.receiveSmsStatus(SmsChannelEnum.YUN_PIAN.getCode(), text);
        return "SUCCESS"; // 约定返回 SUCCESS 为成功
    }

    @PostMapping("/aliyun")
    @PermitAll
    @ApiOperation(value = "阿里云短信的回调", notes = "参见 https://help.aliyun.com/document_detail/120998.html 文档")
    @OperateLog(enable = false)
    public CommonResult<Boolean> receiveAliyunSmsStatus(HttpServletRequest request) throws Throwable {
        String text = ServletUtil.getBody(request);
        smsSendService.receiveSmsStatus(SmsChannelEnum.ALIYUN.getCode(), text);
        return success(true);
    }

    @PostMapping("/tencent")
    @PermitAll
    @ApiOperation(value = "腾讯云短信的回调", notes = "参见 https://cloud.tencent.com/document/product/382/52077 文档")
    @OperateLog(enable = false)
    public CommonResult<Boolean> receiveTencentSmsStatus(HttpServletRequest request) throws Throwable {
        String text = ServletUtil.getBody(request);
        smsSendService.receiveSmsStatus(SmsChannelEnum.TENCENT.getCode(), text);
        return success(true);
    }

}
