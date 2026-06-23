package cn.iocoder.yudao.module.im.dal.dataobject.message.content;

import cn.iocoder.yudao.module.im.enums.ImContentTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * 合并转发消息内容
 * <p>
 * payload 内嵌完整快照，原消息撤回 / 删除不影响已转发的合并消息
 */
@Data
public class MergeMessage {

    /**
     * 合并标题
     */
    private String title;
    /**
     * 内嵌的完整消息快照
     */
    private List<Item> messages;

    /**
     * 单条内嵌消息快照
     */
    @Data
    public static class Item {

        /**
         * 原消息编号
         */
        private Long messageId;
        /**
         * 发送人编号
         */
        private Long senderId;
        /**
         * 发送人昵称快照；接收方可能不在原会话里，无法实时查到
         */
        private String senderNickname;
        /**
         * 发送人头像快照
         */
        private String senderAvatar;
        /**
         * 消息类型
         * <p>
         * 枚举 {@link ImContentTypeEnum}
         */
        private Integer type;
        /**
         * 原消息 content（JSON）
         */
        private String content;
        /**
         * 发送时间戳（毫秒）
         */
        private Long sendTime;

    }

}
