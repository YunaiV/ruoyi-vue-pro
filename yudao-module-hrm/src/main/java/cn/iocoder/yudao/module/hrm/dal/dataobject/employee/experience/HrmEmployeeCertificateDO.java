package cn.iocoder.yudao.module.hrm.dal.dataobject.employee.experience;

import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * HRM 员工证书 DO
 *
 * @author 芋道源码
 */
@TableName("hrm_employee_certificate")
@KeySequence("hrm_employee_certificate_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmEmployeeCertificateDO extends BaseDO {

    /**
     * 证书编号
     */
    @TableId
    private Long id;
    /**
     * 员工编号
     *
     * 关联 {@link HrmEmployeeDO#getId()}
     */
    private Long employeeId;
    /**
     * 证书名称
     */
    private String name;
    /**
     * 证书级别
     */
    private String level;
    /**
     * 证书编号
     */
    private String no;
    /**
     * 有效开始日期
     */
    private LocalDateTime startTime;
    /**
     * 有效结束日期
     */
    private LocalDateTime endTime;
    /**
     * 发证机构
     */
    private String issuingAuthority;
    /**
     * 发证日期
     */
    private LocalDateTime issuingTime;
    /**
     * 备注
     */
    private String remark;
    /**
     * 显示顺序
     */
    private Integer sort;

}
