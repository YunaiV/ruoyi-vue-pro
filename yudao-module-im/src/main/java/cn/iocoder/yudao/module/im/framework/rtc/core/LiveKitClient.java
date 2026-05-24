package cn.iocoder.yudao.module.im.framework.rtc.core;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.signers.JWTSignerUtil;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.module.im.framework.config.ImProperties;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * LiveKit 客户端
 * <p>
 * 厂商绑定层封装：Token 签发（join / admin）、Server API（DeleteRoom / ListParticipants）、identity 拼接 / 解析
 * <p>
 * 由 {@link cn.iocoder.yudao.module.im.framework.rtc.config.ImRtcConfiguration} 注册为 Bean，本类不带 {@code @Component}
 *
 * @author 芋道源码
 */
@Slf4j
public class LiveKitClient {

    /**
     * Twirp 端点路径
     */
    private static final String TWIRP_DELETE_ROOM = "/twirp/livekit.RoomService/DeleteRoom";
    private static final String TWIRP_LIST_PARTICIPANTS = "/twirp/livekit.RoomService/ListParticipants";

    /**
     * 管理 Token 有效期；Server API 一次调用即弃，10 秒足够
     */
    private static final Duration ADMIN_TOKEN_TTL = Duration.ofSeconds(10);

    /**
     * Server API HTTP 调用的超时上限；超时后直接报错，避免 LiveKit 异常 / 网络抖动时业务长时间阻塞
     */
    private static final int SERVER_API_TIMEOUT_MS = 10_000;

    private final ImProperties imProperties;

    /**
     * @param imProperties IM 全局配置；从中读 livekitUrl / apiKey / apiSecret / tokenTtlHours
     */
    public LiveKitClient(ImProperties imProperties) {
        this.imProperties = imProperties;
    }

    /**
     * 签发客户端进房 Token；有效期从 {@link ImProperties.Rtc#getTokenTtlHours()} 读取
     *
     * @param identity    用户唯一标识；写入 sub claim；同 identity 重连会踢前一个连接
     * @param displayName 客户端展示名；可空
     * @param room    房间名
     * @return JWT 字符串
     */
    public String signJoinToken(String identity, String displayName, String room) {
        Assert.notBlank(identity, "identity 不可为空");
        Assert.notBlank(room, "room 不可为空");
        ImProperties.Rtc cfg = imProperties.getRtc();

        // video claim：限定客户端能在该房间内做什么
        Map<String, Object> video = new HashMap<>();
        video.put("roomJoin", true);              // 允许加入房间
        video.put("room", room);              // 限定只能加入这个房间
        video.put("canPublish", true);            // 允许发布媒体（推流）
        video.put("canSubscribe", true);          // 允许订阅媒体（拉流）
        video.put("canPublishData", true);        // 允许发送 data channel 消息

        long nowSec = Instant.now().getEpochSecond();
        long ttlSec = Duration.ofHours(cfg.getTokenTtlHours()).getSeconds();
        JWT jwt = JWT.create()
                .setIssuer(cfg.getApiKey())
                .setSubject(identity)
                .setNotBefore(new Date(nowSec * 1000))
                .setExpiresAt(new Date((nowSec + ttlSec) * 1000))
                .setPayload("video", video)
                .setSigner(JWTSignerUtil.hs256(cfg.getApiSecret().getBytes(StandardCharsets.UTF_8)));
        if (StrUtil.isNotEmpty(displayName)) {
            jwt.setPayload("name", displayName);
        }
        return jwt.sign();
    }

    /**
     * 调用 LiveKit Server API 删除房间：用于通话结束时，强制断开异常残留客户端
     *
     * @param room 房间名
     */
    @SuppressWarnings("EmptyTryBlock")
    public void deleteRoom(String room) {
        try (HttpResponse ignored = postTwirp(TWIRP_DELETE_ROOM, room)) {
            // 状态码不区分；调用方失败即兜底
        }
    }

    /**
     * 调用 LiveKit Server API 查询某房间内的参与者数量：用于定时扫描僵尸通话
     * <p>
     * 房间不存在 LiveKit 返回 404，视同 0 人
     *
     * @param room 房间名
     * @return 参与者数量；HTTP 失败返回 -1
     */
    public int listParticipants(String room) {
        try (HttpResponse response = postTwirp(TWIRP_LIST_PARTICIPANTS, room)) {
            if (response.getStatus() == 404) {
                return 0;
            }
            if (!response.isOk()) {
                log.warn("[listParticipants][LiveKit 返回非 2xx status={} room={} body={}]",
                        response.getStatus(), room, response.body());
                return -1;
            }
            JSONArray participants = JSONUtil.parseObj(response.body()).getJSONArray("participants");
            return CollUtil.size(participants);
        }
    }

    /**
     * 拼接 LiveKit identity；当前单端 = userId 字符串
     * <p>
     * 多端扩展时改 {@code userId + "#" + terminal} 格式，调用方无需改
     *
     * @param userId 用户编号
     * @return identity 字符串
     */
    public String buildIdentity(Long userId) {
        Assert.notNull(userId, "userId 不可为空");
        return String.valueOf(userId);
    }

    /**
     * 从 LiveKit identity 解析业务 userId
     * <p>
     * 当前 identity 直接是 userId 字符串；预留 {@code userId#terminal} 多端格式
     *
     * @param identity LiveKit identity
     * @return 用户编号；解析失败返回 null
     */
    public Long parseUserId(String identity) {
        if (StrUtil.isBlank(identity)) {
            return null;
        }
        int sep = identity.indexOf('#');
        String idPart = sep >= 0 ? identity.substring(0, sep) : identity;
        try {
            return Long.parseLong(idPart);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 校验 LiveKit Webhook 签名；流程参见
     * <a href="https://docs.livekit.io/home/server/webhook/#receiving-webhooks">webhook 文档</a>
     * <p>
     * 校验两步：
     * 1.1 JWT HS256 签名验证；密钥使用 LiveKit API Secret
     * 1.2 body 的 sha256 与 JWT 内 claim 一致；防止抓到 token 后篡改 body
     *
     * @param authHeader 请求头 Authorization 原值（含 "Bearer " 前缀）
     * @param rawBody    请求原始 body
     * @return 是否通过；签名异常一律视为不通过
     */
    public boolean verifyWebhookSignature(String authHeader, String rawBody) {
        if (StrUtil.isBlank(authHeader)) {
            return false;
        }
        String token = StrUtil.removePrefix(authHeader, "Bearer ").trim();
        ImProperties.Rtc cfg = imProperties.getRtc();
        try {
            JWT jwt = JWT.of(token);
            // JWT HS256 签名验证
            if (!jwt.setKey(cfg.getApiSecret().getBytes(StandardCharsets.UTF_8)).verify()) {
                return false;
            }
            // body sha256 一致性校验
            Object expectedSha = jwt.getPayload("sha256");
            if (expectedSha == null) {
                return false;
            }
            // 计算 body 的 sha256，并与 JWT 内 claim 对比
            String actualSha = Base64.encode(DigestUtil.sha256(rawBody));
            return Objects.equals(expectedSha.toString(), actualSha);
        } catch (Exception e) {
            log.warn("[verifyWebhookSignature][签名解析异常 bodyLength={}]",
                    rawBody == null ? 0 : rawBody.length(), e);
            return false;
        }
    }

    /**
     * 签发管理 Token；用于调 Server API（DeleteRoom / ListParticipants / RemoveParticipant 等）
     *
     * @return JWT 字符串
     */
    private String signAdminToken() {
        ImProperties.Rtc cfg = imProperties.getRtc();
        // roomAdmin claim 给管理类 API 必备
        long nowSec = Instant.now().getEpochSecond();
        return JWT.create()
                .setIssuer(cfg.getApiKey())
                .setNotBefore(new Date(nowSec * 1000))
                .setExpiresAt(new Date((nowSec + ADMIN_TOKEN_TTL.getSeconds()) * 1000))
                .setPayload("video", MapUtil.of("roomAdmin", true))
                .setSigner(JWTSignerUtil.hs256(cfg.getApiSecret().getBytes(StandardCharsets.UTF_8)))
                .sign();
    }

    /**
     * Twirp 协议 POST 调用；统一处理 ws→http 协议切换、签 admin token、Bearer 头、JSON body、超时
     *
     * @param path     Twirp 端点路径，例如 {@code /twirp/livekit.RoomService/DeleteRoom}
     * @param room 房间名；写入 JSON body 的 room 字段
     * @return HTTP 响应；调用方负责 close 与状态码判断
     */
    private HttpResponse postTwirp(String path, String room) {
        Assert.notBlank(room, "room 不可为空");
        String token = signAdminToken();
        return HttpRequest.post(HttpUtils.wsUrlToHttp(imProperties.getRtc().getLivekitUrl()) + path)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(MapUtil.of("room", room)))
                .timeout(SERVER_API_TIMEOUT_MS)
                .execute();
    }

}
