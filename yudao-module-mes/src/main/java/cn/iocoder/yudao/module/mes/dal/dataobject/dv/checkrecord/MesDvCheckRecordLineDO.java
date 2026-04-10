package cn.iocoder.yudao.module.mes.dal.dataobject.dv.checkrecord;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.subject.MesDvSubjectDO;
import cn.iocoder.yudao.module.mes.enums.dv.MesDvCheckResultEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * MES 设备点检记录明细 DO
 *
 * @author 芋道源码
 */
@TableName("mes_dv_check_record_line")
@KeySequence("mes_dv_check_record_line_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesDvCheckRecordLineDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 点检记录编号
     *
     * 关联 {@link MesDvCheckRecordDO#getId()}
     */
    private Long recordId;
    /**
     * 点检项目编号
     *
     * 关联 {@link MesDvSubjectDO#getId()}
     */
    private Long subjectId;
    /**
     * 点检结果
     *
     * 枚举 {@link MesDvCheckResultEnum}
     */
    private Integer checkStatus;
    /**
     * 异常描述
     *
     * 仅 {@link #checkStatus} 为异常时使用
     */
    private String checkResult;
    /**
     * 备注
     */
    private String remark;

}
