package cn.iocoder.yudao.module.im.dal.dataobject.group;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.LongListTypeHandler;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IM 群信息 DO
 *
 * @author 芋道源码
 */
@TableName(value = "im_group",autoResultMap = true)
@KeySequence("im_group_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImGroupDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 群名称
     */
    private String name;
    /**
     * 群主用户编号
     * <p>
     * 关联 AdminUserDO 的 id 字段
     */
    private Long ownerUserId;
    /**
     * 群头像
     */
    private String avatar;
    /**
     * 群公告
     */
    private String notice;
    /**
     * 进群是否需群主 / 管理员审批
     * <p>
     * false 自由进群（默认）；true 需审批，所有「申请」「邀请」路径都需群主 / 管理员同意
     */
    private Boolean joinApproval;
    /**
     * 是否封禁
     */
    private Boolean banned;
    /**
     * 封禁原因
     */
    private String bannedReason;
    /**
     * 封禁时间
     */
    private LocalDateTime bannedTime;
    /**
     * 群状态
     * <p>
     * 枚举 {@link CommonStatusEnum}
     * ENABLE = 正常，DISABLE = 已解散
     */
    private Integer status;
    /**
     * 解散时间
     */
    private LocalDateTime dissolvedTime;
    /**
     * 是否全群禁言
     */
    private Boolean mutedAll;
    /**
     * 群置顶消息编号列表
     * <p>
     * 仅存 messageId，操作人 / 置顶时间从对应 PIN 事件的消息记录里反查
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> pinnedMessageIds;

}
