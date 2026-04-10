package cn.iocoder.yudao.module.promotion.dal.dataobject.kefu;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.promotion.enums.kefu.KeFuMessageContentTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 客服消息 DO
 *
 * @author HUIHUI
 */
@TableName("promotion_kefu_message")
@KeySequence("promotion_kefu_message_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeFuMessageDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 会话编号
     *
     * 关联 {@link KeFuConversationDO#getId()}
     */
    private Long conversationId;

    /**
     * 发送人编号
     *
     * 存储的是用户编号
     */
    private Long senderId;
    /**
     * 发送人类型
     *
     * 枚举，{@link UserTypeEnum}
     */
    private Integer senderType;
    /**
     * 接收人编号
     *
     * 存储的是用户编号
     */
    private Long receiverId;
    /**
     * 接收人类型
     *
     * 枚举，{@link UserTypeEnum}
     */
    private Integer receiverType;

    /**
     * 消息类型
     *
     * 枚举 {@link KeFuMessageContentTypeEnum}
     */
    private Integer contentType;
    /**
     * 消息
     */
    private String content;

    //======================= 消息相关状态 =======================

    /**
     * 是/否已读
     */
    private Boolean readStatus;

}
