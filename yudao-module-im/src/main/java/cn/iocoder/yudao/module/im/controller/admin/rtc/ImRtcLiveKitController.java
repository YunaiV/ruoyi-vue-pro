package cn.iocoder.yudao.module.im.controller.admin.rtc;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.im.framework.rtc.core.LiveKitClient;
import cn.iocoder.yudao.module.im.framework.rtc.core.LiveKitWebhookEventDTO;
import cn.iocoder.yudao.module.im.service.rtc.ImRtcCallService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * LiveKit 厂商对接入口（Webhook 回调）
 * <p>
 * 安全由请求签名（JWT + body sha256）保证，不走登录鉴权；伪造请求会被签名校验直接拒绝
 *
 * @author 芋道源码
 */
@Tag(name = "LiveKit Webhook 回调")
@RestController
@RequestMapping("/im/livekit")
@Slf4j
public class ImRtcLiveKitController {

    @Resource
    private LiveKitClient liveKitClient;
    @Resource
    private ImRtcCallService rtcCallService;

    /**
     * LiveKit Webhook 回调入口
     *
     * @param request HTTP 请求；用于取 Authorization 头做签名校验
     * @param rawBody 原始 JSON body；签名校验需要原始字节
     * @return 是否处理成功；伪造 / 非法事件返回 false
     */
    @PostMapping("/webhook")
    @Operation(summary = "LiveKit Webhook 回调；接收成员离开 / 房间结束等事件做业务态兜底清理")
    @PermitAll
    @TenantIgnore
    public CommonResult<Boolean> webhook(HttpServletRequest request, @RequestBody String rawBody) {
        // 1.1 校验签名；伪造请求直接 200 但忽略，避免给攻击者反馈
        if (!liveKitClient.verifyWebhookSignature(request.getHeader("Authorization"), rawBody)) {
            log.warn("[webhook][签名校验失败 ip={} bodyLength={}]",
                    request.getRemoteAddr(), rawBody == null ? 0 : rawBody.length());
            return success(false);
        }
        // 1.2 解析事件载荷；非法 / 空 event 直接忽略
        LiveKitWebhookEventDTO event = JsonUtils.parseObject(rawBody, LiveKitWebhookEventDTO.class);
        if (event == null || StrUtil.isBlank(event.getEvent())) {
            return success(false);
        }

        // 2. 交给 service 处理；幂等由 service 自己保证
        log.info("[webhook][事件处理 event={} room={}]", event.getEvent(),
                event.getRoom() == null ? null : event.getRoom().getName());
        try {
            rtcCallService.handleLiveKitEvent(event);
        } catch (Exception e) {
            log.error("[webhook][事件处理失败 event={} body={}]", event.getEvent(), rawBody, e);
        }
        return success(true);
    }

}
