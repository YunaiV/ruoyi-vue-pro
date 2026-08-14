package cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.config;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.hrm.enums.insurance.config.HrmInsuranceSchemeTypeEnum;
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
 * HRM 社保方案 DO
 *
 * @author 芋道源码
 */
@TableName("hrm_insurance_scheme")
@KeySequence("hrm_insurance_scheme_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmInsuranceSchemeDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 方案名称
     */
    private String name;
    /**
     * 参保地区编号
     */
    private Integer areaId;
    /**
     * 户籍类型
     */
    private String householdType;
    /**
     * 方案类型
     *
     * 枚举 {@link HrmInsuranceSchemeTypeEnum}
     */
    private Integer type;

}
