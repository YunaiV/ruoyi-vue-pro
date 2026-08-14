package cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.post;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.LongListTypeHandler;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.enums.DictTypeConstants;
import cn.iocoder.yudao.module.hrm.enums.recruit.post.HrmRecruitPostStatusEnum;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * HRM 招聘职位 DO
 *
 * @author 芋道源码
 */
@TableName(value = "hrm_recruit_post", autoResultMap = true)
@KeySequence("hrm_recruit_post_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmRecruitPostDO extends BaseDO {

    /**
     * 薪资 - 面议值
     */
    public static final BigDecimal SALARY_NEGOTIABLE_VALUE = BigDecimal.valueOf(-1);
    /**
     * 薪资 - 面议单位值
     */
    public static final Integer SALARY_NEGOTIABLE_UNIT_VALUE = -1;

    /**
     * 年龄 - 不限值
     */
    public static final Integer AGE_UNLIMITED_VALUE = -1;

    /**
     * 招聘职位编号
     */
    @TableId
    private Long id;
    /**
     * 职位名称
     */
    private String postName;
    /**
     * 部门编号
     *
     * 关联 {@link DeptDO#getId()}
     */
    private Long deptId;
    /**
     * 工作性质
     *
     * 字典 {@link DictTypeConstants#HRM_RECRUIT_JOB_NATURE}
     */
    private Integer jobNature;
    /**
     * 工作城市地区编号
     *
     * 关联 {@link cn.iocoder.yudao.framework.ip.core.Area#getId()}
     */
    private Integer areaId;
    /**
     * 招聘人数
     */
    private Integer recruitNum;
    /**
     * 招聘原因
     */
    private String reason;
    /**
     * 工作经验要求
     *
     * 字典 {@link DictTypeConstants#HRM_RECRUIT_WORK_TIME}
     */
    private Integer workTime;
    /**
     * 学历要求
     *
     * 字典 {@link DictTypeConstants#HRM_RECRUIT_POST_EDUCATION}
     */
    private Integer educationRequire;
    /**
     * 最低薪资
     *
     * -1 表示面议
     */
    private BigDecimal minSalary;
    /**
     * 最高薪资
     *
     * -1 表示面议
     */
    private BigDecimal maxSalary;
    /**
     * 薪资单位
     *
     * 字典 {@link DictTypeConstants#HRM_RECRUIT_SALARY_UNIT}
     */
    private Integer salaryUnit;
    /**
     * 最小年龄
     *
     * -1 表示不限
     */
    private Integer minAge;
    /**
     * 最大年龄
     *
     * -1 表示不限
     */
    private Integer maxAge;
    /**
     * 最迟到岗时间
     */
    private LocalDateTime latestEntryTime;
    /**
     * 招聘负责人员工编号
     *
     * 关联 {@link HrmEmployeeDO#getId()}
     */
    private Long ownerEmployeeId;
    /**
     * 面试官员工编号数组
     *
     * 关联 {@link HrmEmployeeDO#getId()}
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> interviewEmployeeIds;
    /**
     * 职位描述
     */
    private String description;
    /**
     * 紧急程度
     *
     * 字典 {@link DictTypeConstants#HRM_RECRUIT_EMERGENCY_LEVEL}
     */
    private Integer emergencyLevel;
    /**
     * 职位类型编号
     *
     * 关联 {@link HrmRecruitPostTypeDO#getId()}
     */
    private Long postTypeId;
    /**
     * 职位状态
     *
     * 枚举 {@link HrmRecruitPostStatusEnum}
     * 字典 {@link DictTypeConstants#HRM_RECRUIT_POST_STATUS}
     */
    private Integer status;
    /**
     * 停止原因
     */
    private String stopReason;

}
