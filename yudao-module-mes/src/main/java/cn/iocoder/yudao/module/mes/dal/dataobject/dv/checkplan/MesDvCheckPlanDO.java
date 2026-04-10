package cn.iocoder.yudao.module.mes.dal.dataobject.dv.checkplan;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;
import cn.iocoder.yudao.module.mes.enums.dv.MesDvCheckPlanStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * MES 点检保养方案 DO
 *
 * @author 芋道源码
 */
@TableName("mes_dv_check_plan")
@KeySequence("mes_dv_check_plan_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesDvCheckPlanDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 方案编码
     */
    private String code;
    /**
     * 方案名称
     */
    private String name;
    /**
     * 方案类型
     *
     * 字典 {@link DictTypeConstants#MES_DV_SUBJECT_TYPE}
     */
    private Integer type;
    /**
     * 开始日期
     */
    private LocalDateTime startDate;
    /**
     * 结束日期
     */
    private LocalDateTime endDate;
    /**
     * 周期类型
     *
     * 字典 {@link DictTypeConstants#MES_DV_CYCLE_TYPE}
     */
    private Integer cycleType;
    /**
     * 周期数量
     */
    private Integer cycleCount;
    /**
     * 状态
     *
     * 枚举 {@link MesDvCheckPlanStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
