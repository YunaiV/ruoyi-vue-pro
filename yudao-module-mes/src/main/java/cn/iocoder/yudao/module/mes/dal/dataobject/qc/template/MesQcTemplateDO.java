package cn.iocoder.yudao.module.mes.dal.dataobject.qc.template;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.IntegerListTypeHandler;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.util.List;

/**
 * MES 质检方案 DO
 *
 * @author 芋道源码
 */
@TableName(value = "mes_qc_template", autoResultMap = true)
@KeySequence("mes_qc_template_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesQcTemplateDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 方案编号
     */
    private String code;
    /**
     * 方案名称
     */
    private String name;
    /**
     * 检测种类
     *
     * 字典 {@link DictTypeConstants#MES_QC_TYPE}
     */
    @TableField(typeHandler = IntegerListTypeHandler.class)
    private List<Integer> types;
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
