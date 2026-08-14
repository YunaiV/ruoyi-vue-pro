package cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.candidate.HrmRecruitCandidateDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.config.HrmRecruitChannelDO;
import cn.iocoder.yudao.module.hrm.enums.DictTypeConstants;
import cn.iocoder.yudao.module.hrm.enums.employee.experience.HrmEmployeeEducationEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeEntryStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeIdTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeStatusEnum;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.enums.common.SexEnum;
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
 * HRM 员工档案 DO
 *
 * @author 芋道源码
 */
@TableName("hrm_employee")
@KeySequence("hrm_employee_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmEmployeeDO extends BaseDO {

    /**
     * 员工编号
     */
    @TableId
    private Long id;
    /**
     * 员工姓名
     */
    private String name;
    /**
     * 工号
     */
    private String jobNumber;
    /**
     * 后台用户编号
     *
     * 关联 {@link AdminUserDO#getId()}
     * 允许为空，用于待入职、离职历史等没有登录账号的员工档案
     */
    private Long userId;
    /**
     * 手机号码
     */
    private String mobile;
    /**
     * 国家或地区
     */
    private String country;
    /**
     * 民族
     */
    private String nation;
    /**
     * 证件类型
     *
     * 枚举 {@link HrmEmployeeIdTypeEnum}
     */
    private Integer idType;
    /**
     * 证件号码
     */
    private String idNumber;
    /**
     * 性别
     *
     * 枚举 {@link SexEnum}
     */
    private Integer sex;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 籍贯
     */
    private String nativePlace;
    /**
     * 出生时间
     */
    private LocalDateTime birthday;
    /**
     * 年龄
     */
    private Integer age;
    /**
     * 户籍地址
     */
    private String address;
    /**
     * 最高学历
     *
     * 枚举 {@link HrmEmployeeEducationEnum}
     * 字典 {@link DictTypeConstants#HRM_EMPLOYEE_EDUCATION}
     */
    private Integer highestEducation;
    /**
     * 部门编号
     *
     * 关联 {@link DeptDO#getId()}
     */
    private Long deptId;
    /**
     * 直属上级员工编号
     *
     * 关联 {@link #id}
     */
    private Long leaderEmployeeId;
    /**
     * 入职状态
     *
     * 枚举 {@link HrmEmployeeEntryStatusEnum}
     * 字典 {@link DictTypeConstants#HRM_EMPLOYEE_ENTRY_STATUS}
     */
    private Integer entryStatus;
    /**
     * 员工状态
     *
     * 枚举 {@link HrmEmployeeStatusEnum}
     * 字典 {@link DictTypeConstants#HRM_EMPLOYEE_STATUS}
     */
    private Integer status;
    /**
     * 聘用形式
     *
     * 枚举 {@link HrmEmployeeTypeEnum}
     * 字典 {@link DictTypeConstants#HRM_EMPLOYEE_TYPE}
     */
    private Integer type;
    /**
     * 入职时间
     */
    private LocalDateTime entryTime;
    /**
     * 试用期，单位：月
     *
     * 0 表示无试用期
     */
    private Integer probation;
    /**
     * 转正时间
     */
    private LocalDateTime regularTime;
    /**
     * 离职时间
     */
    private LocalDateTime leaveTime;
    /**
     * 职位名称
     */
    private String postName;
    /**
     * 岗位职级
     */
    private String postLevel;
    /**
     * 工作城市
     */
    private String workCity;
    /**
     * 工作地址
     */
    private String workAddress;
    /**
     * 工作详细地址
     */
    private String workDetailAddress;
    /**
     * 招聘渠道编号
     *
     * 关联 {@link HrmRecruitChannelDO#getId()}
     */
    private Long channelId;
    /**
     * 司龄开始时间
     */
    private LocalDateTime companyAgeStartTime;
    /**
     * 司龄，单位：年
     */
    private Integer companyAge;
    /**
     * 招聘候选人编号
     *
     * 关联 {@link HrmRecruitCandidateDO#getId()}
     */
    private Long candidateId;
    /**
     * 备注
     */
    private String remark;

}
