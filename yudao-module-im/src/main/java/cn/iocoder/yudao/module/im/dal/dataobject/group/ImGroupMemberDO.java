package cn.iocoder.yudao.module.im.dal.dataobject.group;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.im.enums.group.ImGroupAddSourceEnum;
import cn.iocoder.yudao.module.im.enums.group.ImGroupMemberRoleEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * IM 群成员 DO
 *
 * @author 芋道源码
 */
@TableName("im_group_member")
@KeySequence("im_group_member_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImGroupMemberDO extends BaseDO {

    /**
     * 永久禁言到期时间
     */
    public static final LocalDateTime PERMANENT_MUTE_END_TIME = LocalDateTime.of(9999, 12, 31, 23, 59, 59);

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 群编号
     * <p>
     * 关联 {@link ImGroupDO#getId()}
     */
    private Long groupId;
    /**
     * 用户编号
     * <p>
     * 关联 AdminUserDO 的 id 字段
     */
    private Long userId;
    /**
     * 组内显示名
     */
    private String displayUserName;
    /**
     * 群备注
     */
    private String groupRemark;
    /**
     * 是否免打扰
     */
    private Boolean silent;
    /**
     * 成员状态
     * <p>
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 成员角色
     * <p>
     * 枚举 {@link ImGroupMemberRoleEnum}
     */
    private Integer role;
    /**
     * 入群时间
     */
    private LocalDateTime joinTime;
    /**
     * 加入来源
     * <p>
     * 枚举 {@link ImGroupAddSourceEnum}
     */
    private Integer addSource;
    /**
     * 邀请人用户编号
     * <p>
     * 关联 AdminUserDO 的 id 字段；用户主动申请进群时为 NULL
     */
    private Long inviterUserId;
    /**
     * 退群时间
     */
    private LocalDateTime quitTime;
    /**
     * 禁言到期时间
     * <p>
     * null 表示未禁言；非 null 且在未来表示禁言中；
     */
    private LocalDateTime muteEndTime;

}
