package cn.iocoder.yudao.module.oa.dal.dataobject.schedule;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.oa.enums.schedule.OaPriorityEnum;
import cn.iocoder.yudao.module.oa.enums.schedule.OaScheduleTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * OA 日程 DO
 *
 * @author 芋道源码
 */
@TableName("oa_schedule")
@KeySequence("oa_schedule_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaScheduleDO extends BaseDO {

    /**
     * 日程编号
     */
    @TableId
    private Long id;
    /**
     * 日程类型
     *
     * 枚举 {@link OaScheduleTypeEnum}
     */
    private Integer type;
    /**
     * 优先级
     *
     * 枚举 {@link OaPriorityEnum}
     */
    private Integer priority;
    /**
     * 标题
     */
    private String title;
    /**
     * 描述
     */
    private String description;
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    /**
     * 是否提醒
     */
    private Boolean remind;
    /**
     * 是否已提醒（创建人和参与人全部发送成功后标记）
     */
    private Boolean reminded;

}
