package cn.iocoder.yudao.module.mes.dal.dataobject.dv.repair;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.subject.MesDvSubjectDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * MES 维修工单行 DO
 *
 * @author 芋道源码
 */
@TableName("mes_dv_repair_line")
@KeySequence("mes_dv_repair_line_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesDvRepairLineDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 维修工单编号
     *
     * 关联 {@link MesDvRepairDO#getId()}
     */
    private Long repairId;
    /**
     * 点检保养项目编号
     *
     * 关联 {@link MesDvSubjectDO#getId()}
     */
    private Long subjectId;
    /**
     * 故障描述
     */
    private String malfunction;
    /**
     * 故障图片 URL
     */
    private String malfunctionUrl;
    /**
     * 维修描述
     */
    private String description;
    /**
     * 备注
     */
    private String remark;

}
