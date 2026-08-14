package cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.monthrecord;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.hrm.enums.insurance.monthrecord.HrmInsuranceMonthStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * HRM 月度社保表 DO
 *
 * @author 芋道源码
 */
@TableName("hrm_insurance_month_record")
@KeySequence("hrm_insurance_month_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmInsuranceMonthRecordDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 标题
     */
    private String title;
    /**
     * 年份
     */
    private Integer year;
    /**
     * 月份
     */
    private Integer month;
    /**
     * 参保人数
     */
    private Integer insuredEmployeeCount;
    /**
     * 停保人数
     */
    private Integer stoppedEmployeeCount;
    /**
     * 状态
     *
     * 枚举 {@link HrmInsuranceMonthStatusEnum}
     */
    private Integer status;
    /**
     * 个人社保金额
     */
    private BigDecimal personalInsuranceAmount;
    /**
     * 个人公积金金额
     */
    private BigDecimal personalProvidentFundAmount;
    /**
     * 公司社保金额
     */
    private BigDecimal corporateInsuranceAmount;
    /**
     * 公司公积金金额
     */
    private BigDecimal corporateProvidentFundAmount;

}
