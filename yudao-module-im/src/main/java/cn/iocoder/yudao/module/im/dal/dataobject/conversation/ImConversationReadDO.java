package cn.iocoder.yudao.module.im.dal.dataobject.conversation;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.im.dal.dataobject.channel.ImChannelDO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupDO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImChannelMessageDO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImGroupMessageDO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImPrivateMessageDO;
import cn.iocoder.yudao.module.im.enums.ImConversationTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * IM 会话读位置 DO
 * <p>
 * 只表达「用户在某个会话的最大已读位置」，私聊 / 群聊 / 频道统一落这张表，是读位置的唯一权威。
 *
 * @author 芋道源码
 */
@TableName("im_conversation_read")
@KeySequence("im_conversation_read_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImConversationReadDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 用户编号
     *
     * 关联 AdminUserDO 的 id 字段
     */
    private Long userId;
    /**
     * 会话类型
     * <p>
     * 枚举 {@link ImConversationTypeEnum}
     */
    private Integer conversationType;
    /**
     * 目标编号
     * <p>
     * 私聊关联 AdminUserDO 的 id；群聊关联 {@link ImGroupDO#getId()}；频道关联 {@link ImChannelDO#getId()}
     */
    private Long targetId;
    /**
     * 最大已读消息编号
     * <p>
     * 关联 {@link ImPrivateMessageDO#getId()}、{@link ImGroupMessageDO#getId()}、{@link ImChannelMessageDO#getId()}
     */
    private Long messageId;
    /**
     * 最近已读时间
     */
    private LocalDateTime readTime;

}
