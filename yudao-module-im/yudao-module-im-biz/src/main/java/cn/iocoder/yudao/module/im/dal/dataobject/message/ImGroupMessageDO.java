package cn.iocoder.yudao.module.im.dal.dataobject.message;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupDO;
import cn.iocoder.yudao.module.im.enums.message.ImGroupMessageReceiptStatusEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageStatusEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * IM 群聊消息 DO
 *
 * @author 芋道源码
 */
@TableName("im_group_message")
@KeySequence("im_group_message_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImGroupMessageDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 客户端消息编号，用于发送幂等
     */
    private String clientMessageId;
    /**
     * 发送人编号
     *
     * 关联 AdminUserDO 的 id 字段
     */
    private Long senderId;
    /**
     * 群编号
     *
     * 关联 {@link ImGroupDO#getId()}
     */
    private Long groupId;
    /**
     * 消息类型
     * <p>
     * 枚举 {@link ImMessageTypeEnum}
     */
    private Integer type;
    /**
     * 消息内容，JSON 格式
     *
     * TODO @AI：/Users/yunai/Java/yudao-all-in-im/ruoyi-vue-pro/yudao-module-im/yudao-module-im-biz/src/main/java/cn/iocoder/yudao/module/im/dal/dataobject/message/content
     */
    private String content;
    /**
     * 消息状态
     * <p>
     * 枚举 {@link ImMessageStatusEnum}
     * 群聊状态：NORMAL(0) / RECALL(2)
     */
    // TODO @AI：会不会存在 READ 的情况？如果不存在，最好说明下，和单聊的差异；
    private Integer status;
    /**
     * 发送时间
     */
    private LocalDateTime sendTime;
    /**
     * 定向接收用户编号列表，逗号分隔
     * <p>
     * 为空表示全员可见
     *
     * 关联 AdminUserDO 的 id 字段
     */
    // TODO @AI：使用 List<Long> 存储，避免逗号分隔的字符串解析问题
    private String receiverUserIds;
    /**
     * @ 目标用户编号列表，逗号分隔
     *
     * 关联 AdminUserDO 的 id 字段
     */
    // TODO @AI：使用 List<Long> 存储，避免逗号分隔的字符串解析问题
    private String atUserIds;
    /**
     * 回执状态
     * <p>
     * 枚举 {@link ImGroupMessageReceiptStatusEnum}
     */
    private Integer receiptStatus;

}
