package cn.iocoder.yudao.module.im.framework.rtc.core;

import cn.hutool.core.lang.Assert;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.signers.JWTSignerUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// TODO @AI：是不是搞成 LiveKitClient；然后 config 类去创建起来；通过 @Resource 注入进去？
/**
 * LiveKit Access Token 签发工具
 * <p>
 * LiveKit 协议把权限放在 video claim 里；这里只暴露通话场景需要的最小权限：roomJoin / canPublish /
 * canSubscribe / canPublishData，避免 PoC 阶段误开 roomAdmin。生产想细分会议主持人时再扩字段。
 *
 * @author 芋道源码
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LiveKitTokenUtils {

    /**
     * 签发客户端进房 Token
     *
     * @param apiKey      LiveKit API Key；写入 iss claim
     * @param apiSecret   LiveKit API Secret；HS256 签名密钥；长度必须 ≥ 32
     * @param identity    用户唯一标识；写入 sub claim；同 identity 重连会踢前一个连接
     * @param displayName 客户端展示名；可空
     * @param roomName    房间名
     * @param ttl         有效期
     * @return JWT 字符串
     */
    public static String signJoinToken(String apiKey, String apiSecret, String identity,
                                       String displayName, String roomName, Duration ttl) {
        Assert.notBlank(apiKey, "apiKey 不可为空");
        Assert.notBlank(apiSecret, "apiSecret 不可为空");
        Assert.isTrue(apiSecret.length() >= 32, "apiSecret 长度需 ≥ 32 位");
        Assert.notBlank(identity, "identity 不可为空");
        Assert.notBlank(roomName, "roomName 不可为空");
        Assert.notNull(ttl, "ttl 不可为空");

        // TODO @AI：这里注释下；
        Map<String, Object> video = new HashMap<>();
        video.put("roomJoin", true);  // TODO @AI：这里帮忙注释下
        video.put("room", roomName);  // TODO @AI：这里帮忙注释下
        video.put("canPublish", true); // TODO @AI：这里帮忙注释下
        video.put("canSubscribe", true);  // TODO @AI：这里帮忙注释下
        video.put("canPublishData", true);  // TODO @AI：这里帮忙注释下

        // TODO @AI：这里注释下；
        // TODO @AI：是不是 new Date；然后下面去处理，更合理？或者 System currentTime 这种？
        long now = Instant.now().getEpochSecond();
        JWT jwt = JWT.create()
                .setIssuer(apiKey)
                .setSubject(identity)
                .setNotBefore(new Date(now * 1000))
                .setExpiresAt(new Date((now + ttl.getSeconds()) * 1000))
                .setPayload("video", video)
                .setSigner(JWTSignerUtil.hs256(apiSecret.getBytes()));
        // TODO @AI：empty 处理下；
        if (displayName != null && !displayName.isEmpty()) {
            jwt.setPayload("name", displayName);
        }
        return jwt.sign();
    }

}
