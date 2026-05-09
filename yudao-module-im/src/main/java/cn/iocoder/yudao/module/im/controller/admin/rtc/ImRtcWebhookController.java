package cn.iocoder.yudao.module.im.controller.admin.rtc;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.im.framework.config.ImProperties;
import cn.iocoder.yudao.module.im.service.rtc.ImRtcCallService;
import cn.iocoder.yudao.module.im.service.rtc.dto.LiveKitWebhookEventDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

// TODO @AI：是不是改成 ImRtcLiveKitController 更合适？？？
/**
 * LiveKit Webhook 入口
 * <p>
 * 安全由请求签名（JWT + body sha256）保证，不走登录鉴权；伪造请求会被签名校验直接拒绝
 *
 * @author 芋道源码
 */
@Tag(name = "LiveKit Webhook 回调")
@RestController
@RequestMapping("/im/livekit")
@Slf4j
public class ImRtcWebhookController {

    @Resource
    private ImProperties imProperties;
    @Resource
    private ImRtcCallService rtcCallService;

    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "LiveKit Webhook 回调；接收成员离开 / 房间结束等事件做业务态兜底清理")
    @PermitAll
    public CommonResult<Boolean> webhook(HttpServletRequest request, @RequestBody String rawBody) {
        // TODO @AI：1.1 1.2 这种
        // 1. 校验签名；伪造请求直接 200 但忽略，避免给攻击者反馈
        if (!verifySignature(request, rawBody)) {
            log.warn("[livekit-webhook][签名校验失败 ip={} body={}]", request.getRemoteAddr(), rawBody);
            return success(false);
        }
        // 2. 解析事件并交给 service 处理；幂等由 service 自己保证
        LiveKitWebhookEventDTO event = JsonUtils.parseObject(rawBody, LiveKitWebhookEventDTO.class);
        if (event == null || StrUtil.isBlank(event.getEvent())) {
            return success(false);
        }

        // TODO @AI：2. ；这里是不是要打个 logger info？
        try {
            rtcCallService.handleLiveKitEvent(event);
        } catch (Exception e) {
            log.error("[livekit-webhook][事件处理失败 event={} body={}]", event.getEvent(), rawBody, e);
        }
        return success(true);
    }

    // TODO @AI：是不是给 LiveKitClient 校验？
    /**
     * 校验 LiveKit Webhook 签名；流程参见
     * https://docs.livekit.io/home/server/webhook/#receiving-webhooks
     */
    private boolean verifySignature(HttpServletRequest request, String rawBody) {
        String authHeader = request.getHeader("Authorization");
        if (StrUtil.isBlank(authHeader)) {
            return false;
        }
        String token = StrUtil.removePrefix(authHeader, "Bearer ").trim();
        ImProperties.Rtc cfg = imProperties.getRtc();
        try {
            JWT jwt = JWT.of(token);
            // 1. JWT HS256 签名；密钥使用 LiveKit API Secret
            if (!jwt.setKey(cfg.getApiSecret().getBytes(StandardCharsets.UTF_8)).verify()) {
                return false;
            }
            // 2. body sha256 一致性校验；防止抓到 token 后篡改 body
            Object expectedSha = jwt.getPayload("sha256");
            if (expectedSha == null) {
                return false;
            }
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawBody.getBytes(StandardCharsets.UTF_8));
            String actualSha = Base64.encode(digest);
            return Objects.equals(expectedSha.toString(), actualSha);
        } catch (Exception e) {
            // TODO @AI：是不是要加个什么 request 的参数，方便溯源？
            log.warn("[livekit-webhook][签名解析异常]", e);
            return false;
        }
    }

}
