package cn.iocoder.yudao.module.im.framework.rtc.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * LiveKit Webhook 事件载荷；只反序列化我们关心的字段，其余忽略
 * <p>
 * 文档参考：<a href="https://docs.livekit.io/home/server/webhook/">webhook 文档</a>
 *
 * @author 芋道源码
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LiveKitWebhookEventDTO {

    /**
     * 房间创建：首个 participant 加入时触发
     */
    public static final String EVENT_ROOM_STARTED = "room_started";
    /**
     * 房间销毁：最后一个 participant 离开 / 显式 DeleteRoom 后触发；用于业务侧兜底关房
     */
    public static final String EVENT_ROOM_FINISHED = "room_finished";
    /**
     * 参与者加入房间
     */
    public static final String EVENT_PARTICIPANT_JOINED = "participant_joined";
    /**
     * 参与者离开房间：关 tab / 网络断 / 显式 disconnect 都会触发；用于业务侧兜底清理
     */
    public static final String EVENT_PARTICIPANT_LEFT = "participant_left";
    /**
     * 参与者发布媒体轨道（摄像头 / 麦克风 / 屏幕共享）
     */
    public static final String EVENT_TRACK_PUBLISHED = "track_published";

    /**
     * 事件 id：用于幂等去重
     */
    private String id;

    /**
     * 事件类型；取值参见本类 EVENT_* 常量
     */
    private String event;

    /**
     * 房间元信息：room_started / room_finished 必填
     */
    private RoomInfo room;

    /**
     * 参与者元信息：participant_* 事件必填
     */
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
        /**
         * 用户身份：签 token 时写入 userId 字符串，后续支持多端会拼成 userId#terminal
         */
        private String identity;
        private String name;
        private Long joinedAt;

    }

}
