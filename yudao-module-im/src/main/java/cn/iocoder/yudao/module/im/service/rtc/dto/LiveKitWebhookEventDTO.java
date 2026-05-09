package cn.iocoder.yudao.module.im.service.rtc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

// TODO @AI：注释风格；（这个是不是 VO 哈。不放在 VO ）
/**
 * LiveKit Webhook 事件载荷；只反序列化我们关心的字段，其余忽略
 * <p>
 * 文档参考：https://docs.livekit.io/home/server/webhook/
 *
 * @author 芋道源码
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LiveKitWebhookEventDTO {

    /** 事件类型；room_started / room_finished / participant_joined / participant_left / track_published 等 */
    private String event;

    /** 事件 id；用于幂等去重 */
    private String id;

    /** 房间元信息；room_started / room_finished 必填 */
    private RoomInfo room;

    /** 参与者元信息；participant_* 事件必填 */
    private ParticipantInfo participant;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RoomInfo {
        private String sid;
        private String name;
        private Long creationTime;
        private Integer numParticipants;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParticipantInfo {
        private String sid;
        /** 用户身份；我们签 token 时写入 userId 字符串，后续支持多端会拼成 userId#terminal */
        private String identity;
        private String name;
        private Long joinedAt;
    }

}
