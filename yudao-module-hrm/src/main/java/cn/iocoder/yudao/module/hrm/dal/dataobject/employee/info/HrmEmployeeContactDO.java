package cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info;

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

/**
 * HRM 员工联系人 DO
 *
 * @author 芋道源码
 */
@TableName("hrm_employee_contact")
@KeySequence("hrm_employee_contact_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmEmployeeContactDO extends BaseDO {

    /**
     * 联系人编号
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
     * 联系人名称
     */
    private String name;
    /**
     * 与员工的关系
     */
    private String relation;
    /**
     * 联系人电话
     */
    private String phone;
    /**
     * 联系人工作单位
     */
    private String workUnit;
    /**
     * 联系人职务
     */
    private String postName;
    /**
     * 联系人地址
     */
    private String address;
    /**
     * 显示顺序
     */
    private Integer sort;

}
