package cn.iocoder.yudao.module.mes.dal.dataobject.qc.defect;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * MES 缺陷类型 DO
 *
 * @author 芋道源码
 */
@TableName("mes_qc_defect")
@KeySequence("mes_qc_defect_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesQcDefectDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 缺陷编码
     */
    private String code;
    /**
     * 缺陷描述
     */
    private String name;
    /**
     * 检测项类型
     *
     * 字典类型 {@link cn.iocoder.yudao.module.mes.enums.DictTypeConstants#MES_DEFECT_TYPE}
     */
    private String type;
    /**
     * 缺陷等级
     *
     * 枚举 {@link cn.iocoder.yudao.module.mes.enums.qc.MesQcDefectLevelEnum}
     */
    private Integer level;
    /**
     * 备注
     */
    private String remark;

}
