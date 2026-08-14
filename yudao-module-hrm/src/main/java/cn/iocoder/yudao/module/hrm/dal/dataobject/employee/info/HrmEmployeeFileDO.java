package cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info;

import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeFileTypeEnum;
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
 * HRM 员工材料附件 DO
 *
 * @author 芋道源码
 */
@TableName("hrm_employee_file")
@KeySequence("hrm_employee_file_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmEmployeeFileDO extends BaseDO {

    /**
     * 附件编号
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
     * 附件类型
     *
     * 枚举 {@link HrmEmployeeFileTypeEnum}
     */
    private Integer type;
    /**
     * 附件地址
     */
    private String url;

}
