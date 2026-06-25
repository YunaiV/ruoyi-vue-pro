package cn.iocoder.yudao.module.im.dal.dataobject.rtc;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.im.enums.rtc.ImRtcParticipantRoleEnum;
import cn.iocoder.yudao.module.im.enums.rtc.ImRtcParticipantStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * IM 通话参与者 DO（用户级 / 明细表）
 * <p>
 * 一通通话每个参与者一行；通过 {@link #room} 关联主表 {@link ImRtcCallDO}
 * <p>
 * 终态闭合：通话 ENDED 时所有明细 status 必属 {LEFT / REJECTED / NO_ANSWER} 之一
 *
 * @author 芋道源码
 */
@TableName("im_rtc_participant")
@KeySequence("im_rtc_participant_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增；MySQL 等数据库可不写
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImRtcParticipantDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 通话编号
     * <p>
     * 关联 {@link ImRtcCallDO#getId()}
     */
    private Long callId;
    /**
     * 业务通话编号
     * <p>
     * 关联 {@link ImRtcCallDO#getRoom()}
     */
    private String room;
    /**
     * 参与者用户编号
     * <p>
     * 关联 AdminUserDO 的 id 字段
     */
    private Long userId;
    /**
     * 参与角色
     *
     * 枚举 {@link ImRtcParticipantRoleEnum}
     */
    private Integer role;
    /**
     * 参与状态
     *
     * 枚举 {@link ImRtcParticipantStatusEnum}
     */
    private Integer status;
    /**
     * 被邀请时间；发起人取通话 startTime
     */
    private LocalDateTime inviteTime;
    /**
     * 接听时间；未接听 NULL
     */
    private LocalDateTime acceptTime;
    /**
     * 离开时间；未加入 NULL
     */
    private LocalDateTime leaveTime;

}
