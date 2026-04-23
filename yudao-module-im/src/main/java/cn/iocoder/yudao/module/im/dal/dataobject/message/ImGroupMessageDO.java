package cn.iocoder.yudao.module.im.dal.dataobject.message;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.LongListTypeHandler;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupDO;
import cn.iocoder.yudao.module.im.enums.message.ImGroupMessageReceiptStatusEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageStatusEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IM 群聊消息 DO
 *
 * @author 芋道源码
 */
@TableName(value = "im_group_message", autoResultMap = true)
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
     * 参考 content 包下的 TextMessage、ImageMessage 等结构化模型
     */
    private String content;
    /**
     * 消息状态
     * <p>
     * 枚举 {@link ImMessageStatusEnum}
     *
     * 为什么没有 READ 状态？与单聊的差异：单聊用 UNREAD/READ 跟踪每条消息状态，群聊用 Redis 存储每个成员的已读位置（游标模型）
     */
    private Integer status;
    /**
     * 发送时间
     */
    private LocalDateTime sendTime;
    /**
     * 定向接收用户编号列表，以逗号分隔
     * <p>
     * 为空表示全员可见
     *
     * 关联 AdminUserDO 的 id 字段
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> receiverUserIds;
    /**
     * @ 目标用户编号列表，以逗号分隔
     *
     * 关联 AdminUserDO 的 id 字段
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> atUserIds;
    /**
     * 回执状态
     * <p>
     * 枚举 {@link ImGroupMessageReceiptStatusEnum}
     */
    private Integer receiptStatus;

    // ========== 非表字段 ==========

    /**
     * 离线拉取等场景下回算的已读人数
     */
    @TableField(exist = false)
    private Integer readCount;

}
