package cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * MES 计量单位 DO
 *
 * @author 芋道源码
 */
@TableName("mes_md_unit_measure")
@KeySequence("mes_md_unit_measure_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesMdUnitMeasureDO extends BaseDO {

    /**
     * 单位编号
     */
    @TableId
    private Long id;
    /**
     * 单位编码
     */
    private String code;
    /**
     * 单位名称
     */
    private String name;
    /**
     * 是否主单位
     */
    private Boolean primaryFlag;
    /**
     * 主单位编号
     *
     * 关联 {@link MesMdUnitMeasureDO#getId()}
     */
    private Long primaryId;
    /**
     * 与主单位换算比例
     */
    private BigDecimal changeRate;
    /**
     * 状态
     *
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
