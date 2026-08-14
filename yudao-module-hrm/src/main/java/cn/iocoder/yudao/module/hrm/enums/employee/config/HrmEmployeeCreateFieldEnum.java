package cn.iocoder.yudao.module.hrm.enums.employee.config;

import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeEntryStatusEnum;
import cn.hutool.core.util.ArrayUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * HRM 新建员工字段枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmEmployeeCreateFieldEnum {

    NAME("name", "员工姓名", "个人信息", true, true, true, true),
    USER_ID("userId", "绑定用户", "个人信息", true, false, true, false),
    MOBILE("mobile", "手机号", "个人信息", true, true, true, true),
    EMAIL("email", "邮箱", "个人信息", true, false, true, false),
    COUNTRY("country", "国家或地区", "个人信息", false, false, false, false),
    NATION("nation", "民族", "个人信息", false, false, false, false),
    ID_TYPE("idType", "证件类型", "个人信息", true, false, true, false),
    ID_NUMBER("idNumber", "证件号码", "个人信息", true, false, true, false),
    SEX("sex", "性别", "个人信息", true, false, true, false),
    NATIVE_PLACE("nativePlace", "籍贯", "个人信息", false, false, false, false),
    BIRTHDAY("birthday", "出生日期", "个人信息", false, false, false, false),
    AGE("age", "年龄", "个人信息", false, false, false, false),
    HIGHEST_EDUCATION("highestEducation", "最高学历", "个人信息", false, false, false, false),
    ADDRESS("address", "户籍地址", "个人信息", false, false, false, false),

    JOB_NUMBER("jobNumber", "工号", "入职信息", true, true, false, false),
    ENTRY_STATUS("entryStatus", "入职状态", "入职信息", true, true, true, true),
    DEPT_ID("deptId", "部门", "入职信息", true, false, true, false),
    POST_NAME("postName", "职位名称", "入职信息", true, false, true, false),
    POST_LEVEL("postLevel", "岗位职级", "入职信息", false, false, false, false),
    LEADER_EMPLOYEE_ID("leaderEmployeeId", "直属上级", "入职信息", true, false, true, false),
    CHANNEL_ID("channelId", "招聘渠道", "入职信息", false, false, false, false),
    TYPE("type", "聘用形式", "入职信息", true, true, true, true),
    STATUS("status", "员工状态", "入职信息", true, true, true, true),
    ENTRY_TIME("entryTime", "入职时间", "入职信息", true, true, true, true),
    PROBATION("probation", "试用期（月）", "入职信息", true, true, true, true),
    REGULAR_TIME("regularTime", "转正时间", "入职信息", false, false, false, false),
    LEAVE_TIME("leaveTime", "离职时间", "入职信息", false, false, false, false),
    COMPANY_AGE_START_TIME("companyAgeStartTime", "司龄起算时间", "入职信息",
            false, false, false, false),
    COMPANY_AGE("companyAge", "司龄（年）", "入职信息", false, false, false, false),
    WORK_CITY("workCity", "工作城市", "入职信息", true, false, true, false),
    WORK_ADDRESS("workAddress", "工作地点", "入职信息", false, false, false, false),
    WORK_DETAIL_ADDRESS("workDetailAddress", "工作详细地址", "入职信息",
            false, false, false, false),
    CANDIDATE_ID("candidateId", "招聘候选人", "入职信息", false, false, false, false),
    REMARK("remark", "备注", "入职信息", true, false, true, false);

    /**
     * 字段名称
     */
    private final String name;
    /**
     * 字段标题
     */
    private final String title;
    /**
     * 字段分组名称
     */
    private final String groupName;
    /**
     * 在职办理时默认是否显示
     */
    private final Boolean activeDefaultVisible;
    /**
     * 在职办理时是否锁定显示
     */
    private final Boolean activeVisibleLocked;
    /**
     * 待入职办理时默认是否显示
     */
    private final Boolean pendingEntryDefaultVisible;
    /**
     * 待入职办理时是否锁定显示
     */
    private final Boolean pendingEntryVisibleLocked;

    public static HrmEmployeeCreateFieldEnum valueOfName(String name) {
        return ArrayUtil.firstMatch(item -> item.getName().equals(name), values());
    }

    /**
     * 获得指定入职状态的默认显隐值
     *
     * @param entryStatus 入职状态
     * @return 默认是否显示
     */
    public Boolean getDefaultVisible(Integer entryStatus) {
        return HrmEmployeeEntryStatusEnum.ACTIVE.getStatus().equals(entryStatus)
                ? activeDefaultVisible : pendingEntryDefaultVisible;
    }

    /**
     * 获得指定入职状态的锁定显示值
     *
     * @param entryStatus 入职状态
     * @return 是否锁定显示
     */
    public Boolean getVisibleLocked(Integer entryStatus) {
        return HrmEmployeeEntryStatusEnum.ACTIVE.getStatus().equals(entryStatus)
                ? activeVisibleLocked : pendingEntryVisibleLocked;
    }

}
