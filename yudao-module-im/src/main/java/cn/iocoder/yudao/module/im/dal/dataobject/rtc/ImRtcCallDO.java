package cn.iocoder.yudao.module.im.dal.dataobject.rtc;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupDO;
import cn.iocoder.yudao.module.im.enums.ImConversationTypeEnum;
import cn.iocoder.yudao.module.im.enums.rtc.ImRtcCallEndReasonEnum;
import cn.iocoder.yudao.module.im.enums.rtc.ImRtcCallMediaTypeEnum;
import cn.iocoder.yudao.module.im.enums.rtc.ImRtcCallStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * IM 通话记录 DO（房间级 / 主表）
 * <p>
 * 一通通话一行；状态机 CREATED → RUNNING → ENDED；和明细表 {@link ImRtcParticipantDO} 通过 {@link #room} 关联
 *
 * @author 芋道源码
 */
@TableName("im_rtc_call")
@KeySequence("im_rtc_call_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增；MySQL 等数据库可不写
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImRtcCallDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 业务通话编号（UUID，同时作为 LiveKit 房间名）；唯一
     */
    private String room;
    /**
     * 会话类型
     *
     * 枚举 {@link ImConversationTypeEnum}
     */
    private Integer conversationType;
    /**
     * 媒体类型
     *
     * 枚举 {@link ImRtcCallMediaTypeEnum}
     */
    private Integer mediaType;
    /**
     * 发起人用户编号
     * <p>
     * 关联 AdminUserDO 的 id 字段
     */
    private Long inviterUserId;
    /**
     * 群编号；私聊为 NULL
     * <p>
     * 关联 {@link ImGroupDO#getId()}
     */
    private Long groupId;
    /**
     * 通话状态
     *
     * 枚举 {@link ImRtcCallStatusEnum}
     */
    private Integer status;
    /**
     * 结束原因；通话未结束时为 NULL
     *
     * 枚举 {@link ImRtcCallEndReasonEnum}
     */
    private Integer endReason;
    /**
     * 发起时间
     */
    private LocalDateTime startTime;
    /**
     * 接通时间
     *
     * 首个非发起人加入时写入；未接通保持 NULL
     */
    private LocalDateTime acceptTime;
    /**
     * 通话结束时间
     */
    private LocalDateTime endTime;

}
