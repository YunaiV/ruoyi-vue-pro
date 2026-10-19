package cn.iocoder.yudao.module.oa.dal.dataobject.schedule;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * OA 日程参与人 DO
 *
 * @author 芋道源码
 */
@TableName("oa_schedule_participant")
@KeySequence("oa_schedule_participant_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class OaScheduleParticipantDO extends BaseDO {

    /**
     * 参与记录编号
     */
    @TableId
    private Long id;
    /**
     * 日程编号
     *
     * 关联 {@link OaScheduleDO#getId()}
     */
    private Long scheduleId;
    /**
     * 参与人用户编号
     *
     * 关联 {@link AdminUserDO#getId()}
     */
    private Long userId;
    /**
     * 是否已读
     */
    private Boolean readStatus;
    /**
     * 首次阅读时间
     */
    private LocalDateTime readTime;

}
