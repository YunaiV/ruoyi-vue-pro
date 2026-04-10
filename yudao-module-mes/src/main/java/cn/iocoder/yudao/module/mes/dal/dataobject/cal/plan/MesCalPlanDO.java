package cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.enums.cal.MesCalPlanStatusEnum;
import cn.iocoder.yudao.module.mes.enums.cal.MesCalShiftMethodEnum;
import cn.iocoder.yudao.module.mes.enums.cal.MesCalShiftTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * MES 排班计划 DO
 *
 * @author 芋道源码
 */
@TableName("mes_cal_plan")
@KeySequence("mes_cal_plan_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesCalPlanDO extends BaseDO {

    /**
     * 计划编号
     */
    @TableId
    private Long id;
    /**
     * 计划编码
     */
    private String code;
    /**
     * 计划名称
     */
    private String name;
    /**
     * 班组类型
     */
    private Integer calendarType;
    /**
     * 开始日期
     */
    private LocalDateTime startDate;
    /**
     * 结束日期
     */
    private LocalDateTime endDate;
    /**
     * 轮班方式
     *
     * 枚举 {@link MesCalShiftTypeEnum}
     */
    private Integer shiftType;
    /**
     * 倒班方式
     *
     * 枚举 {@link MesCalShiftMethodEnum}
     */
    private Integer shiftMethod;
    /**
     * 倒班天数
     */
    private Integer shiftCount;
    /**
     * 状态
     *
     * 枚举 {@link MesCalPlanStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
