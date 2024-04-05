package cn.iocoder.yudao.module.mp.controller.admin.open;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.mp.controller.admin.open.vo.MpOpenCheckSignatureReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.open.vo.MpOpenHandleMessageReqVO;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import cn.iocoder.yudao.module.mp.framework.mp.core.MpServiceFactory;
import cn.iocoder.yudao.module.mp.framework.mp.core.context.MpContextHolder;
import cn.iocoder.yudao.module.mp.service.account.MpAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.Objects;

@Tag(name = "管理后台 - 公众号回调")
@RestController
@RequestMapping("/mp/open")
@Validated
@Slf4j
public class MpOpenController {

    @Resource
    private MpServiceFactory mpServiceFactory;

    @Resource
    private MpAccountService mpAccountService;

    /**
     * 接收微信公众号的校验签名
     *
     * 对应 <a href="https://developers.weixin.qq.com/doc/offiaccount/Basic_Information/Access_Overview.html">文档</a>
     */
    @Operation(summary = "校验签名") // 参见
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
    @Operation(summary = "处理消息")
    @PostMapping(value = "/{appId}", produces = "application/xml; charset=UTF-8")
    public String handleMessage(@PathVariable("appId") String appId,
                                @RequestBody String content,
                                MpOpenHandleMessageReqVO reqVO) {
        log.info("[handleMessage][appId({}) 推送消息，参数({}) 内容({})]", appId, reqVO, content);

        // 处理 appId + 多租户的上下文
        MpAccountDO account = mpAccountService.getAccountFromCache(appId);
        Assert.notNull(account, "公众号 appId({}) 不存在", appId);
        try {
            MpContextHolder.setAppId(appId);
            return TenantUtils.execute(account.getTenantId(),
                    () -> handleMessage0(appId, content, reqVO));
        } finally {
            MpContextHolder.clear();
        }
    }

    private String handleMessage0(String appId, String content, MpOpenHandleMessageReqVO reqVO) {
        // 校验请求签名
        WxMpService mppService = mpServiceFactory.getRequiredMpService(appId);
        Assert.isTrue(mppService.checkSignature(reqVO.getTimestamp(), reqVO.getNonce(), reqVO.getSignature()),
                "非法请求");

        // 第一步，解析消息
        WxMpXmlMessage inMessage = null;
        if (StrUtil.isBlank(reqVO.getEncrypt_type())) { // 明文模式
            inMessage = WxMpXmlMessage.fromXml(content);
        } else if (Objects.equals(reqVO.getEncrypt_type(), MpOpenHandleMessageReqVO.ENCRYPT_TYPE_AES)) { // AES 加密模式
            inMessage = WxMpXmlMessage.fromEncryptedXml(content, mppService.getWxMpConfigStorage(),
                    reqVO.getTimestamp(), reqVO.getNonce(), reqVO.getMsg_signature());
        }
        Assert.notNull(inMessage, "消息解析失败，原因：消息为空");

        // 第二步，处理消息
        WxMpMessageRouter mpMessageRouter = mpServiceFactory.getRequiredMpMessageRouter(appId);
        WxMpXmlOutMessage outMessage = mpMessageRouter.route(inMessage);
        if (outMessage == null) {
            return "";
        }

        // 第三步，返回消息
        if (StrUtil.isBlank(reqVO.getEncrypt_type())) { // 明文模式
            return outMessage.toXml();
        } else if (Objects.equals(reqVO.getEncrypt_type(), MpOpenHandleMessageReqVO.ENCRYPT_TYPE_AES)) { // AES 加密模式
            return outMessage.toEncryptedXml(mppService.getWxMpConfigStorage());
        }
        return "";
    }

}
