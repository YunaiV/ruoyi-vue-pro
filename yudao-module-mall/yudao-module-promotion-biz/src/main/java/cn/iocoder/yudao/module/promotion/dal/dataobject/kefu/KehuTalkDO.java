package cn.iocoder.yudao.module.promotion.dal.dataobject.kefu;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.promotion.enums.kehu.MessageTypeEnum;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 客户会话 DO
 *
 * @author HUIHUI
 */
@TableName("kehu_talk")
@KeySequence("kehu_talk_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KehuTalkDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 会话所属用户
     *
     * 关联 {@link MemberUserRespDTO#getId()}
     */
    private Long userId;
    /**
     * 用户名称
     *
     * 关联 {@link MemberUserRespDTO#getNickname()}
     */
    private String userName;
    /**
     * 用户头像
     *
     * 关联 {@link MemberUserRespDTO#getAvatar()}
     */
    private String userFace;
    /**
     * 管理员名称
     *
     * 关联 {@link AdminUserRespDTO#getNickname()}
     */
    private String adminName;
    /**
     * 管理员头像, 管理员搞个默认头像
     */
    private String adminFace;

    /**
     * 最后聊天时间
     */
    private LocalDateTime lastTalkTime;
    /**
     * 最后聊天内容
     */
    private String lastTalkMessage;
    /**
     * 最后发送的消息类型
     *
     * 枚举 {@link MessageTypeEnum}
     */
    private Integer lastMessageType;

    //======================= 会话操作相关 =======================

    /**
     * 管理端置顶
     */
    private Boolean adminPinned;
    /**
     * 用户端不可见，默认为 true
     *
     * 用户删除此会话时设置为 false
     */
    private Boolean userDisable;
    /**
     * 管理员端不可见，默认为 true
     *
     * 管理员删除此会话时设置为 false
     */
    private Boolean adminDisable;

    /**
     * 管理员未读消息数
     *
     * 用户发送消息时增加，管理员查看后扣减
     */
    private Integer adminUnreadMessageCount;

}
