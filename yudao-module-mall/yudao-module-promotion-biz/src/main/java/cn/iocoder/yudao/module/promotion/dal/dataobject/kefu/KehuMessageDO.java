package cn.iocoder.yudao.module.promotion.dal.dataobject.kefu;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.promotion.enums.kehu.MessageTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 客户消息 DO
 *
 * @author HUIHUI
 */
@TableName("kehu_message")
@KeySequence("kehu_message_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KehuMessageDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 会话编号
     *
     * 关联 {@link KehuTalkDO#getId()}
     */
    private Long talkId;

    /**
     * 发送者
     */
    private Long fromUserId;
    /**
     * 发送者用户类型
     */
    private String fromUserType;
    /**
     * 接收者
     */
    private Long toUserId;
    /**
     * 接收着用户类型
     */
    private String toUserType;

    /**
     * 消息类型
     *
     * 枚举 {@link MessageTypeEnum}
     */
    private Integer messageType;
    /**
     * 消息
     */
    private String message;

    //======================= 消息相关状态 =======================

    /**
     * 是/否已读
     */
    private Boolean isRead;

}
