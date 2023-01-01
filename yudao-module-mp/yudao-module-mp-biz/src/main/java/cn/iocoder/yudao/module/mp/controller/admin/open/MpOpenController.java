package cn.iocoder.yudao.module.mp.controller.admin.open;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.mp.controller.admin.open.vo.MpOpenCheckSignatureReqVO;
import cn.iocoder.yudao.module.mp.framework.mp.core.MpServiceFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api(tags = "管理后台 - 公众号回调")
@RestController
@RequestMapping("/mp/open")
@Validated
@Slf4j
public class MpOpenController {

    @Resource
    private MpServiceFactory mpServiceFactory;

    /**
     * 接收微信公众号的校验签名
     *
     * 对应 <a href="https://developers.weixin.qq.com/doc/offiaccount/Basic_Information/Access_Overview.html">文档</a>
     */
    @ApiOperation("校验签名") // 参见
    @GetMapping(value = "/{appId}", produces = "text/plain;charset=utf-8")
    public String checkSignature(@PathVariable("appId") String appId,
                                 MpOpenCheckSignatureReqVO reqVO) {
        log.info("[checkSignature][appId({}) 接收到来自微信服务器的认证消息({})]", appId, reqVO);
        // 校验请求签名
        WxMpService wxMpService = mpServiceFactory.getRequiredMpService(appId);
        // 校验通过
        if (wxMpService.checkSignature(reqVO.getTimestamp(), reqVO.getNonce(), reqVO.getSignature())) {
            return reqVO.getEchostr();
        }
        // 校验不通过
        return "非法请求";
    }

    /**
     * 接收微信公众号的消息推送
     *
     * <a href="https://developers.weixin.qq.com/doc/offiaccount/Message_Management/Receiving_standard_messages.html">文档</a>
     */
    @ApiOperation("处理消息")
    @PostMapping(value = "/{appId}", produces = "application/xml; charset=UTF-8")
    public String handleMessage() {
        return "123";
    }

}
