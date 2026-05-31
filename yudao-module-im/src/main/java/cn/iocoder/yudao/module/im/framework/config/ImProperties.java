package cn.iocoder.yudao.module.im.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * IM 模块全局配置
 * <p>
 * 各子模块用嵌套 inner class 区分（friend / group / face / message / rtc 等），
 * yaml 路径保持 yudao.im.{module}.{key} 与原有部署保持兼容
 *
 * @author 芋道源码
 */
@Component
@ConfigurationProperties(prefix = "yudao.im")
@Validated
@Data
public class ImProperties {

    private Friend friend = new Friend();

    private Group group = new Group();

    private Face face = new Face();

    private Message message = new Message();

    @Valid
    private Rtc rtc = new Rtc();

    /**
     * 好友模块配置
     */
    @Data
    public static class Friend {

        /**
         * 是否自动通过所有好友申请（全局开关）
         * <p>
         * 默认 false，普通用户必须走申请-审批流程；开启后所有用户的好友申请会立即同意，主要用于全员开放型 IM 部署。
         * 如需细化到「仅特定用户自动通过」（如机器人 / AI 账号），请在 system 用户表加字段，并在 applyFriend 内按用户级开关短路
         */
        private boolean autoAccept = false;

    }

    /**
     * 群模块配置
     */
    @Data
    public static class Group {

        /**
         * 群最大成员人数
         */
        private int maxMember = 500;

        /**
         * 单群管理员人数上限
         */
        private int adminMaxCount = 3;

        /**
         * 单群置顶消息条数上限
         */
        private int pinMaxCount = 5;

    }

    /**
     * 表情模块配置
     */
    @Data
    public static class Face {

        /**
         * 个人表情数量上限
         */
        private int userItemMaxCount = 200;

    }

    /**
     * 消息模块配置
     */
    @Data
    public static class Message {

        /**
         * 是否启用私聊已读功能
         * <p>
         * 关闭后：private read 接口直接抛业务异常；服务端不再下发私聊 READ / RECEIPT 事件信号。
         * 客户端侧需镜像此开关，隐藏私聊气泡的「已读 / 未读」标签
         */
        private boolean privateReadEnabled = true;

        /**
         * 是否启用群聊已读功能（含群消息回执）
         * <p>
         * 关闭后：group read 接口直接抛业务异常；服务端不再下发群 READ / RECEIPT 事件信号；
         * 群消息回执 receiptStatus 一并停用（即使发送方传 receipt=true 也强制落 NO_RECEIPT，不再算「N 人已读」）。
         * 客户端侧需镜像此开关，隐藏群回执 popover 与「发送回执消息」入口
         */
        private boolean groupReadEnabled = true;

        /**
         * pull 最大拉取数量
         */
        private int maxPullSize = 1000;

        /**
         * 消息撤回时间限制（分钟）
         */
        private int recallTimeoutMinutes = 5;

        /**
         * 私聊离线消息最大拉取天数
         * <p>
         * 客户端通过 pull 接口增量拉取私聊离线消息时，仅返回最近 N 天内产生的消息，
         * 超过该窗口的老消息不再主动推送（可通过历史消息接口按需倒翻）。
         */
        private int privatePullMaxDays = 30;

        /**
         * 群聊离线消息最大拉取天数
         * <p>
         * 客户端通过 pull 接口增量拉取群聊离线消息时，仅返回最近 N 天内产生的消息；
         * 退群前消息的补齐也以该窗口为基准（早于窗口的退群群不再扫描），避免老用户首次
         * 拉取时对历史退群群做大量查询。
         */
        private int groupPullMaxDays = 30;

    }

    /**
     * 实时通话模块配置
     * <p>
     * 媒体走 LiveKit SFU；后端只签 Token + 通过 IM 长连接推送来电 / 接通 / 结束三种信令。
     * 关闭后所有 RTC 接口直接抛 RTC_NOT_ENABLED；前端可据此隐藏通话按钮。
     */
    @Data
    public static class Rtc {

        /**
         * 是否启用实时通话功能
         */
        private boolean enabled = true;

        /**
         * LiveKit Server WebSocket 地址；客户端 connect 时使用，通常 ws://host:7880 或 wss://host
         */
        @NotBlank(message = "LiveKit URL 不能为空")
        private String livekitUrl = "ws://127.0.0.1:7880";

        /**
         * LiveKit API Key
         */
        @NotBlank(message = "LiveKit API Key 不能为空")
        private String apiKey = "devkey";

        /**
         * LiveKit API Secret；生产必须改为强随机值
         */
        @NotBlank(message = "LiveKit API Secret 不能为空")
        @Size(min = 32, message = "LiveKit API Secret 长度需 ≥ 32 位")
        private String apiSecret = "secret-poc-key-min-32-chars-required-here";

        /**
         * 单次签发的 Token 有效期（小时）
         */
        private int tokenTtlHours = 6;

        /**
         * 群通话最大同时在房成员数；超过 invite 直接拒绝
         */
        private int groupMaxParticipants = 16;

        /**
         * 僵尸通话清理阈值（分钟）；通话创建超过此值仍未结束才纳入扫描，避开「刚发起还在响铃」的合理零人态
         */
        private int cleanupZombieThresholdMinutes = 5;

        /**
         * 振铃超时阈值（分钟）；被叫 INVITING 超过此值未接通 → 标 NO_ANSWER + 推 RTC_CALL(REJECT) 让 banner 收敛
         */
        private int inviteTimeoutMinutes = 1;

    }

}
