package cn.iocoder.yudao.module.fms.dal.dataobject.report;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * FMS 报表模板 DO
 *
 * @author 芋道源码
 */
@TableName("fms_report_template")
@KeySequence("fms_report_template_seq")
@TenantIgnore
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FmsReportTemplateDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 配置名称
     */
    private String name;
    /**
     * 行次
     */
    private Integer rowNo;
    /**
     * 公式
     */
    private String formula;
    /**
     * 备注
     */
    private String remark;
    /**
     * 是否可编辑
     */
    private Boolean editable;
    /**
     * 显示顺序
     */
    private Integer sort;
    /**
     * 行编号
     */
    private Integer rowId;
    /**
     * 类型
     *
     * 枚举 {@link cn.iocoder.yudao.module.fms.enums.report.FmsReportTypeEnum}
     */
    private Integer type;
    /**
     * 现金流量表分类
     */
    private Integer category;
    /**
     * 层级
     */
    private Integer level;

}
