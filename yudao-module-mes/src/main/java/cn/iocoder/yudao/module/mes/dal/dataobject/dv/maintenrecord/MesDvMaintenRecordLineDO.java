package cn.iocoder.yudao.module.mes.dal.dataobject.dv.maintenrecord;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.subject.MesDvSubjectDO;
import cn.iocoder.yudao.module.mes.enums.dv.MesDvMaintenStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;

/**
 * MES 设备保养记录明细 DO
 *
 * @author 芋道源码
 */
@TableName("mes_dv_mainten_record_line")
@KeySequence("mes_dv_mainten_record_line_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesDvMaintenRecordLineDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 保养记录编号
     *
     * 关联 {@link MesDvMaintenRecordDO#getId()}
     */
    private Long recordId;
    /**
     * 项目编号
     *
     * 关联 {@link MesDvSubjectDO#getId()}
     */
    private Long subjectId;
    /**
     * 保养结果
     *
     * 字典 {@link DictTypeConstants#MES_MAINTEN_STATUS}
     * 枚举 {@link MesDvMaintenStatusEnum}
     */
    private Integer status;
    /**
     * 异常描述
     */
    private String result;
    /**
     * 备注
     */
    private String remark;

}
