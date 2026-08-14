package cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment;

import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.StringListTypeHandler;
import cn.iocoder.yudao.module.hrm.enums.employee.employment.HrmEmployeeContractStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.employment.HrmEmployeeContractTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * HRM 员工合同 DO
 *
 * @author 芋道源码
 */
@TableName(value = "hrm_employee_contract", autoResultMap = true)
@KeySequence("hrm_employee_contract_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmEmployeeContractDO extends BaseDO {

    /**
     * 合同编号
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
     * 合同编号
     */
    private String no;
    /**
     * 合同类型
     *
     * 枚举 {@link HrmEmployeeContractTypeEnum}
     */
    private Integer type;
    /**
     * 合同开始日期
     */
    private LocalDateTime startTime;
    /**
     * 合同结束日期
     */
    private LocalDateTime endTime;
    /**
     * 合同期限，单位：年
     */
    private Integer term;
    /**
     * 合同状态
     *
     * 枚举 {@link HrmEmployeeContractStatusEnum}
     */
    private Integer status;
    /**
     * 签约公司
     */
    private String signCompany;
    /**
     * 合同签订日期
     */
    private LocalDateTime signTime;
    /**
     * 备注
     */
    private String remark;
    /**
     * 是否到期提醒
     *
     */
    private Boolean expireRemind;
    /**
     * 显示顺序
     */
    private Integer sort;
    /**
     * 附件地址数组
     */
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> fileUrls;

}
