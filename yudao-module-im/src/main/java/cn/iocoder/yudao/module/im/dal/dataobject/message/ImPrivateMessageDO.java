package cn.iocoder.yudao.module.im.dal.dataobject.message;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.im.enums.message.ImMessageReceiptStatusEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageStatusEnum;
import cn.iocoder.yudao.module.im.enums.ImContentTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * IM 私聊消息 DO
 *
 * @author 芋道源码
 */
@TableName("im_private_message")
@KeySequence("im_private_message_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImPrivateMessageDO extends BaseDO {

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
     * 接收人编号
     *
     * 关联 AdminUserDO 的 id 字段
     */
    private Long receiverId;
    /**
     * 消息类型
     * <p>
     * 枚举 {@link ImContentTypeEnum}
     */
    private Integer type;
    /**
     * 消息内容，JSON 格式
     *
     * 参考 content 包下的 TextMessage、ImageMessage 等结构化模型
     */
    private String content;
    /**
     * 消息状态
     * <p>
     * 枚举 {@link ImMessageStatusEnum}
     */
    private Integer status;
    /**
     * 回执状态
     * <p>
     * 枚举 {@link ImMessageReceiptStatusEnum}
     */
    private Integer receiptStatus;
    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

}
