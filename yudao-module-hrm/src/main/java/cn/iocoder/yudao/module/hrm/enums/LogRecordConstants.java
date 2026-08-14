package cn.iocoder.yudao.module.hrm.enums;

/**
 * HRM 操作日志常量
 *
 * @author 芋道源码
 */
public interface LogRecordConstants {

    // ======================= HRM_EMPLOYEE 员工档案 =======================

    String HRM_EMPLOYEE_TYPE = "HRM 员工档案";
    String HRM_EMPLOYEE_CREATE_SUB_TYPE = "创建员工档案";
    String HRM_EMPLOYEE_CREATE_SUCCESS = "创建了员工档案【{{#employee.name}}】";
    String HRM_EMPLOYEE_CREATE_FROM_USER_LIST_SUB_TYPE = "从后台用户创建员工档案";
    String HRM_EMPLOYEE_ARCHIVE_FILL_MESSAGE_SUB_TYPE = "发送填写员工档案通知";
    String HRM_EMPLOYEE_UPDATE_SUB_TYPE = "更新员工档案";
    String HRM_EMPLOYEE_UPDATE_SUCCESS = "更新了员工档案【{{#updateReqVO.name}}】: {_DIFF{#updateReqVO}}";
    String HRM_EMPLOYEE_PROFILE_UPDATE_SUB_TYPE = "员工更新个人档案";
    String HRM_EMPLOYEE_PROFILE_UPDATE_SUCCESS =
            "员工更新了个人档案【{{#updateReqVO.name}}】: {_DIFF{#updateReqVO}}";
    String HRM_EMPLOYEE_DELETE_SUB_TYPE = "删除员工档案";
    String HRM_EMPLOYEE_DELETE_SUCCESS = "删除了员工档案【{{#employee.name}}】";
    String HRM_EMPLOYEE_FILE_UPDATE_SUB_TYPE = "更新员工材料附件";
    String HRM_EMPLOYEE_FILE_UPDATE_SUCCESS = "更新了员工档案【{{#employee.name}}】的材料附件";
    String HRM_EMPLOYEE_CONFIRM_ENTRY_SUB_TYPE = "确认员工入职";
    String HRM_EMPLOYEE_CONFIRM_ENTRY_SUCCESS = "确认员工【{{#employee.name}}】入职";
    String HRM_EMPLOYEE_REHIRE_SUB_TYPE = "办理员工再入职";
    String HRM_EMPLOYEE_REHIRE_SUCCESS = "办理员工【{{#employee.name}}】再入职";
    String HRM_EMPLOYEE_REGULAR_SUB_TYPE = "办理员工转正";
    String HRM_EMPLOYEE_REGULAR_SUCCESS = "办理员工【{{#employee.name}}】转正，生效时间："
            + "【{{#reqVO.effectTime}}】";
    String HRM_EMPLOYEE_TRANSFER_SUB_TYPE = "办理员工调岗";
    String HRM_EMPLOYEE_TRANSFER_SUCCESS = "办理员工【{{#employee.name}}】调岗，生效时间："
            + "【{{#reqVO.effectTime}}】";
    String HRM_EMPLOYEE_PROMOTE_SUB_TYPE = "办理员工晋升";
    String HRM_EMPLOYEE_PROMOTE_SUCCESS = "办理员工【{{#employee.name}}】晋升，生效时间："
            + "【{{#reqVO.effectTime}}】";
    String HRM_EMPLOYEE_DEMOTE_SUB_TYPE = "办理员工降级";
    String HRM_EMPLOYEE_DEMOTE_SUCCESS = "办理员工【{{#employee.name}}】降级，生效时间："
            + "【{{#reqVO.effectTime}}】";
    String HRM_EMPLOYEE_FULL_TIME_SUB_TYPE = "办理员工转为全职";
    String HRM_EMPLOYEE_FULL_TIME_SUCCESS = "办理员工【{{#employee.name}}】转为全职，生效时间："
            + "【{{#reqVO.effectTime}}】";
    String HRM_EMPLOYEE_QUIT_SUB_TYPE = "设置员工离职";
    String HRM_EMPLOYEE_QUIT_SUCCESS = "设置员工【{{#employee.name}}】离职，计划离职时间："
            + "【{{#reqVO.planQuitTime}}】";
    String HRM_EMPLOYEE_CANCEL_QUIT_SUB_TYPE = "取消员工离职";
    String HRM_EMPLOYEE_CANCEL_QUIT_SUCCESS = "取消员工【{{#employee.name}}】的离职安排，原因："
            + "【{{#reqVO.reason}}】";
    String HRM_EMPLOYEE_SALARY_CARD_SAVE_SUB_TYPE = "保存员工工资卡";
    String HRM_EMPLOYEE_SALARY_CARD_SAVE_SUCCESS =
            "保存了员工【{{#employee.name}}】的工资卡: {_DIFF{#reqVO}}";
    String HRM_EMPLOYEE_SALARY_CARD_DELETE_SUB_TYPE = "删除员工工资卡";
    String HRM_EMPLOYEE_SALARY_CARD_DELETE_SUCCESS = "删除了员工【{{#employee.name}}】的工资卡";
    String HRM_EMPLOYEE_EDUCATION_EXPERIENCE_CREATE_SUB_TYPE = "新增员工教育经历";
    String HRM_EMPLOYEE_EDUCATION_EXPERIENCE_CREATE_SUCCESS =
            "新增了员工教育经历【{{#educationExperience.id}}】";
    String HRM_EMPLOYEE_EDUCATION_EXPERIENCE_UPDATE_SUB_TYPE = "更新员工教育经历";
    String HRM_EMPLOYEE_EDUCATION_EXPERIENCE_UPDATE_SUCCESS =
            "更新了员工教育经历【{{#reqVO.id}}】: {_DIFF{#reqVO}}";
    String HRM_EMPLOYEE_EDUCATION_EXPERIENCE_DELETE_SUB_TYPE = "删除员工教育经历";
    String HRM_EMPLOYEE_EDUCATION_EXPERIENCE_DELETE_SUCCESS =
            "删除了员工教育经历【{{#educationExperience.id}}】";
    String HRM_EMPLOYEE_WORK_EXPERIENCE_CREATE_SUB_TYPE = "新增员工工作经历";
    String HRM_EMPLOYEE_WORK_EXPERIENCE_CREATE_SUCCESS =
            "新增了员工工作经历【{{#workExperience.id}}】";
    String HRM_EMPLOYEE_WORK_EXPERIENCE_UPDATE_SUB_TYPE = "更新员工工作经历";
    String HRM_EMPLOYEE_WORK_EXPERIENCE_UPDATE_SUCCESS =
            "更新了员工工作经历【{{#reqVO.id}}】: {_DIFF{#reqVO}}";
    String HRM_EMPLOYEE_WORK_EXPERIENCE_DELETE_SUB_TYPE = "删除员工工作经历";
    String HRM_EMPLOYEE_WORK_EXPERIENCE_DELETE_SUCCESS =
            "删除了员工工作经历【{{#workExperience.id}}】";
    String HRM_EMPLOYEE_TRAINING_EXPERIENCE_CREATE_SUB_TYPE = "新增员工培训经历";
    String HRM_EMPLOYEE_TRAINING_EXPERIENCE_CREATE_SUCCESS =
            "新增了员工培训经历【{{#trainingExperience.id}}】";
    String HRM_EMPLOYEE_TRAINING_EXPERIENCE_UPDATE_SUB_TYPE = "更新员工培训经历";
    String HRM_EMPLOYEE_TRAINING_EXPERIENCE_UPDATE_SUCCESS =
            "更新了员工培训经历【{{#reqVO.id}}】: {_DIFF{#reqVO}}";
    String HRM_EMPLOYEE_TRAINING_EXPERIENCE_DELETE_SUB_TYPE = "删除员工培训经历";
    String HRM_EMPLOYEE_TRAINING_EXPERIENCE_DELETE_SUCCESS =
            "删除了员工培训经历【{{#trainingExperience.id}}】";
    String HRM_EMPLOYEE_CERTIFICATE_CREATE_SUB_TYPE = "新增员工证书";
    String HRM_EMPLOYEE_CERTIFICATE_CREATE_SUCCESS = "新增了员工证书【{{#certificate.id}}】";
    String HRM_EMPLOYEE_CERTIFICATE_UPDATE_SUB_TYPE = "更新员工证书";
    String HRM_EMPLOYEE_CERTIFICATE_UPDATE_SUCCESS =
            "更新了员工证书【{{#reqVO.id}}】: {_DIFF{#reqVO}}";
    String HRM_EMPLOYEE_CERTIFICATE_DELETE_SUB_TYPE = "删除员工证书";
    String HRM_EMPLOYEE_CERTIFICATE_DELETE_SUCCESS = "删除了员工证书【{{#certificate.id}}】";
    String HRM_EMPLOYEE_CONTACT_CREATE_SUB_TYPE = "新增员工联系人";
    String HRM_EMPLOYEE_CONTACT_CREATE_SUCCESS = "新增了员工联系人【{{#contact.id}}】";
    String HRM_EMPLOYEE_CONTACT_UPDATE_SUB_TYPE = "更新员工联系人";
    String HRM_EMPLOYEE_CONTACT_UPDATE_SUCCESS =
            "更新了员工联系人【{{#reqVO.id}}】: {_DIFF{#reqVO}}";
    String HRM_EMPLOYEE_CONTACT_DELETE_SUB_TYPE = "删除员工联系人";
    String HRM_EMPLOYEE_CONTACT_DELETE_SUCCESS = "删除了员工联系人【{{#contact.id}}】";
    String HRM_EMPLOYEE_CONTRACT_CREATE_SUB_TYPE = "新增员工合同";
    String HRM_EMPLOYEE_CONTRACT_CREATE_SUCCESS = "新增了员工合同【{{#contract.id}}】";
    String HRM_EMPLOYEE_CONTRACT_UPDATE_SUB_TYPE = "更新员工合同";
    String HRM_EMPLOYEE_CONTRACT_UPDATE_SUCCESS =
            "更新了员工合同【{{#reqVO.id}}】: {_DIFF{#reqVO}}";
    String HRM_EMPLOYEE_CONTRACT_DELETE_SUB_TYPE = "删除员工合同";
    String HRM_EMPLOYEE_CONTRACT_DELETE_SUCCESS = "删除了员工合同【{{#contract.id}}】";

    // ======================= HRM_EMPLOYEE_CONFIG 员工字段配置 =======================

    String HRM_EMPLOYEE_CONFIG_TYPE = "HRM 员工字段配置";
    String HRM_EMPLOYEE_CREATE_FIELD_CONFIG_UPDATE_SUB_TYPE = "更新新建员工字段配置";
    String HRM_EMPLOYEE_CREATE_FIELD_CONFIG_UPDATE_SUCCESS =
            "更新了【{{#configGroupName}}】: {_DIFF{#reqVO}}";
    String HRM_EMPLOYEE_ARCHIVE_FIELD_CONFIG_UPDATE_SUB_TYPE = "更新员工档案字段配置";
    String HRM_EMPLOYEE_ARCHIVE_FIELD_CONFIG_UPDATE_SUCCESS =
            "更新了员工档案字段配置: {_DIFF{#reqVO}}";
    // ======================= HRM_INSURANCE_EMPLOYEE_INFO 员工参保信息 =======================

    String HRM_INSURANCE_EMPLOYEE_INFO_TYPE = "HRM 员工参保信息";
    String HRM_INSURANCE_EMPLOYEE_INFO_SAVE_SUB_TYPE = "保存员工参保信息";
    String HRM_INSURANCE_EMPLOYEE_INFO_SAVE_SUCCESS =
            "保存了员工【{{#employee.name}}】的参保信息: {_DIFF{#reqVO}}";
    String HRM_INSURANCE_EMPLOYEE_INFO_UPDATE_SCHEME_SUB_TYPE = "设置员工参保方案";
    String HRM_INSURANCE_EMPLOYEE_INFO_UPDATE_SCHEME_SUCCESS = "将员工【{{#employee.name}}】的参保方案从"
            + "【{{#oldSchemeName}}】变更为【{{#newSchemeName}}】";

    // ======================= HRM_INSURANCE_SCHEME 社保方案 =======================

    String HRM_INSURANCE_SCHEME_TYPE = "HRM 社保方案";
    String HRM_INSURANCE_SCHEME_CREATE_SUB_TYPE = "创建社保方案";
    String HRM_INSURANCE_SCHEME_CREATE_SUCCESS = "创建了社保方案【{{#insuranceScheme.name}}】";
    String HRM_INSURANCE_SCHEME_UPDATE_SUB_TYPE = "更新社保方案";
    String HRM_INSURANCE_SCHEME_UPDATE_SUCCESS = "更新了社保方案【{{#insuranceScheme.name}}】";
    String HRM_INSURANCE_SCHEME_DELETE_SUB_TYPE = "删除社保方案";
    String HRM_INSURANCE_SCHEME_DELETE_SUCCESS = "删除了社保方案【{{#insuranceScheme.name}}】";

    // ======================= HRM_INSURANCE_MONTH 月度社保表 =======================

    String HRM_INSURANCE_MONTH_TYPE = "HRM 月度社保表";
    String HRM_INSURANCE_MONTH_CREATE_SUB_TYPE = "创建月度社保表";
    String HRM_INSURANCE_MONTH_CREATE_SUCCESS = "创建了月度社保表【{{#monthRecord.title}}】";
    String HRM_INSURANCE_MONTH_DELETE_SUB_TYPE = "删除月度社保表";
    String HRM_INSURANCE_MONTH_DELETE_SUCCESS = "删除了月度社保表【{{#monthRecord.title}}】";
    String HRM_INSURANCE_MONTH_EMPLOYEE_CREATE_SUB_TYPE = "添加月度参保员工";
    String HRM_INSURANCE_MONTH_EMPLOYEE_CREATE_SUCCESS = "向月度社保表【{{#monthRecord.title}}】添加了"
            + "【{{#reqVO.employeeIds.size()}}】名员工";
    String HRM_INSURANCE_MONTH_EMPLOYEE_UPDATE_SUB_TYPE = "调整员工月度参保方案";
    String HRM_INSURANCE_MONTH_EMPLOYEE_UPDATE_SUCCESS = "将员工月度社保记录【{{#employeeRecord.id}}】调整为"
            + "参保方案【{{#scheme.name}}】";
    String HRM_INSURANCE_MONTH_EMPLOYEE_STOP_SUB_TYPE = "批量停止员工参保";
    String HRM_INSURANCE_MONTH_EMPLOYEE_STOP_SUCCESS = "停止了月度社保表【{{#monthRecord.title}}】中的"
            + "【{{#ids.size()}}】名员工参保";

    // ======================= HRM_ATTENDANCE_GROUP 考勤组 =======================

    String HRM_ATTENDANCE_GROUP_TYPE = "HRM 考勤组";
    String HRM_ATTENDANCE_GROUP_CREATE_SUB_TYPE = "创建考勤组";
    String HRM_ATTENDANCE_GROUP_CREATE_SUCCESS = "创建了考勤组【{{#attendanceGroup.name}}】";
    String HRM_ATTENDANCE_GROUP_UPDATE_SUB_TYPE = "更新考勤组";
    String HRM_ATTENDANCE_GROUP_UPDATE_SUCCESS =
            "更新了考勤组【{{#updateReqVO.name}}】: {_DIFF{#updateReqVO}}";
    String HRM_ATTENDANCE_GROUP_DELETE_SUB_TYPE = "删除考勤组";
    String HRM_ATTENDANCE_GROUP_DELETE_SUCCESS = "删除了考勤组【{{#attendanceGroup.name}}】";

    // ======================= HRM_RECRUIT_POST 招聘职位 =======================

    String HRM_RECRUIT_POST_TYPE = "HRM 招聘职位";
    String HRM_RECRUIT_POST_CREATE_SUB_TYPE = "创建招聘职位";
    String HRM_RECRUIT_POST_CREATE_SUCCESS = "创建了招聘职位【{{#createReqVO.postName}}】";
    String HRM_RECRUIT_POST_UPDATE_SUB_TYPE = "更新招聘职位";
    String HRM_RECRUIT_POST_UPDATE_SUCCESS = "更新了招聘职位【{{#updateReqVO.postName}}】: {_DIFF{#updateReqVO}}";
    String HRM_RECRUIT_POST_UPDATE_STATUS_SUB_TYPE = "更新招聘职位状态";
    String HRM_RECRUIT_POST_UPDATE_STATUS_SUCCESS = "将招聘职位【{{#recruitPost.postName}}】的状态从"
            + "【{getRecruitPostStatusName{#recruitPost.status}}】变更为了"
            + "【{getRecruitPostStatusName{#reqVO.status}}】";

    // ======================= HRM_RECRUIT_CHANNEL 招聘渠道 =======================

    String HRM_RECRUIT_CHANNEL_TYPE = "HRM 招聘渠道";
    String HRM_RECRUIT_CHANNEL_CREATE_SUB_TYPE = "创建招聘渠道";
    String HRM_RECRUIT_CHANNEL_CREATE_SUCCESS = "创建了招聘渠道【{{#createReqVO.name}}】";
    String HRM_RECRUIT_CHANNEL_UPDATE_SUB_TYPE = "更新招聘渠道";
    String HRM_RECRUIT_CHANNEL_UPDATE_SUCCESS = "更新了招聘渠道【{{#updateReqVO.name}}】: {_DIFF{#updateReqVO}}";
    String HRM_RECRUIT_CHANNEL_UPDATE_STATUS_SUB_TYPE = "更新招聘渠道状态";
    String HRM_RECRUIT_CHANNEL_UPDATE_STATUS_SUCCESS = "将招聘渠道【{{#recruitChannel.name}}】的状态从"
            + "【{getHrmCommonStatusName{#recruitChannel.status}}】变更为了"
            + "【{getHrmCommonStatusName{#statusReqVO.status}}】";
    String HRM_RECRUIT_CHANNEL_DELETE_SUB_TYPE = "删除招聘渠道";
    String HRM_RECRUIT_CHANNEL_DELETE_SUCCESS = "删除了招聘渠道【{{#recruitChannel.name}}】，并将关联员工和候选人"
            + "转移至【{{#transferRecruitChannel.name}}】";

    // ======================= HRM_RECRUIT_CONFIG 招聘设置 =======================

    String HRM_RECRUIT_CONFIG_TYPE = "HRM 招聘设置";
    String HRM_RECRUIT_ELIMINATE_REASON_UPDATE_SUB_TYPE = "更新招聘淘汰原因";
    String HRM_RECRUIT_ELIMINATE_REASON_UPDATE_SUCCESS =
            "更新了招聘淘汰原因: {_DIFF{#saveReqVO}}";

    // ======================= HRM_RECRUIT_CANDIDATE 招聘候选人 =======================

    String HRM_RECRUIT_CANDIDATE_TYPE = "HRM 招聘候选人";
    String HRM_RECRUIT_CANDIDATE_CREATE_SUB_TYPE = "创建招聘候选人";
    String HRM_RECRUIT_CANDIDATE_CREATE_SUCCESS = "创建了招聘候选人【{{#createReqVO.name}}】";
    String HRM_RECRUIT_CANDIDATE_UPDATE_SUB_TYPE = "更新招聘候选人";
    String HRM_RECRUIT_CANDIDATE_UPDATE_SUCCESS = "更新了招聘候选人【{{#updateReqVO.name}}】: {_DIFF{#updateReqVO}}";
    String HRM_RECRUIT_CANDIDATE_DELETE_SUB_TYPE = "删除招聘候选人";
    String HRM_RECRUIT_CANDIDATE_DELETE_SUCCESS = "删除了招聘候选人【{{#recruitCandidate.name}}】";
    String HRM_RECRUIT_CANDIDATE_UPDATE_STATUS_SUB_TYPE = "更新招聘候选人状态";
    String HRM_RECRUIT_CANDIDATE_UPDATE_STATUS_SUCCESS = "将招聘候选人【{{#recruitCandidate.name}}】的状态从"
            + "【{getRecruitCandidateStatusName{#recruitCandidate.status}}】变更为了"
            + "【{getRecruitCandidateStatusName{#reqVO.status}}】";
    String HRM_RECRUIT_CANDIDATE_ARRANGE_INTERVIEW_SUB_TYPE = "安排招聘面试";
    String HRM_RECRUIT_CANDIDATE_ARRANGE_INTERVIEW_SUCCESS = "为招聘候选人【{{#recruitCandidate.name}}】安排了第"
            + "【{{#recruitInterview.stageNumber}}】轮面试";
    String HRM_RECRUIT_CANDIDATE_UPDATE_INTERVIEW_SUB_TYPE = "更改招聘面试安排";
    String HRM_RECRUIT_CANDIDATE_UPDATE_INTERVIEW_SUCCESS = "更改了招聘候选人"
            + "【{{#recruitCandidate.name}}】第【{{#recruitInterview.stageNumber}}】轮面试安排: {_DIFF{#updateReqVO}}";
    String HRM_RECRUIT_CANDIDATE_INTERVIEW_RESULT_SUB_TYPE =
            "{{#reqVO.result == 2 ? '面试通过' : (#reqVO.result == 3 ? '面试未通过' : "
                    + "(#reqVO.result == 4 ? '取消面试' : '登记面试结果'))}}";
    String HRM_RECRUIT_CANDIDATE_INTERVIEW_RESULT_SUCCESS = "将招聘候选人【{{#recruitCandidate.name}}】第"
            + "【{{#recruitInterview.stageNumber}}】轮面试结果设置为"
            + "【{getRecruitInterviewResultName{#reqVO.result}}】"
            + "{{#reqVO.result == 4 ? '，取消原因：【' + #reqVO.cancelReason + '】' : "
            + "(#reqVO.evaluate != null && !#reqVO.evaluate.isEmpty() ? '，面试评价：【' + #reqVO.evaluate + '】' : '')}}";
    String HRM_RECRUIT_CANDIDATE_UPDATE_POST_SUB_TYPE = "更新招聘候选人应聘职位";
    String HRM_RECRUIT_CANDIDATE_UPDATE_POST_SUCCESS = "将招聘候选人【{{#recruitCandidate.name}}】的应聘职位变更为"
            + "【{{#recruitPost.postName}}】";
    String HRM_RECRUIT_CANDIDATE_UPDATE_CHANNEL_SUB_TYPE = "更新招聘候选人招聘渠道";
    String HRM_RECRUIT_CANDIDATE_UPDATE_CHANNEL_SUCCESS = "将招聘候选人【{{#recruitCandidate.name}}】的招聘渠道变更为"
            + "【{{#recruitChannel.name}}】";
    String HRM_RECRUIT_CANDIDATE_ELIMINATE_SUB_TYPE = "淘汰招聘候选人";
    String HRM_RECRUIT_CANDIDATE_ELIMINATE_SUCCESS = "淘汰了招聘候选人【{{#recruitCandidate.name}}】，原因："
            + "【{{#reqVO.eliminate}}】";
    String HRM_RECRUIT_CANDIDATE_CONVERT_EMPLOYEE_SUB_TYPE = "招聘候选人转员工";
    String HRM_RECRUIT_CANDIDATE_CONVERT_EMPLOYEE_SUCCESS = "将招聘候选人【{{#recruitCandidate.name}}】转为员工，"
            + "员工编号为【{{#employeeId}}】";

    // ======================= HRM_SALARY_MONTH 月度工资表 =======================

    String HRM_SALARY_MONTH_TYPE = "HRM 月度工资表";
    String HRM_SALARY_MONTH_CREATE_SUB_TYPE = "创建月度工资表";
    String HRM_SALARY_MONTH_CREATE_SUCCESS = "创建了月度工资表【{{#salaryMonthRecord.title}}】";
    String HRM_SALARY_MONTH_COMPUTE_SUB_TYPE = "核算月度工资表";
    String HRM_SALARY_MONTH_COMPUTE_SUCCESS = "核算了月度工资表【{{#salaryMonthRecord.title}}】";
    String HRM_SALARY_MONTH_DELETE_SUB_TYPE = "删除月度工资表";
    String HRM_SALARY_MONTH_DELETE_SUCCESS = "删除了月度工资表【{{#salaryMonthRecord.title}}】";
    String HRM_SALARY_MONTH_EMPLOYEE_UPDATE_SUB_TYPE = "在线修改员工月度工资";
    String HRM_SALARY_MONTH_EMPLOYEE_UPDATE_SUCCESS =
            "在线修改了月度工资表【{{#salaryMonthRecord.title}}】，影响【{{#employeeCount}}】名员工，"
                    + "员工编号【{{#employeeScope}}】，变更薪资项【{{#changedOptionSummary}}】";

    // ======================= HRM_SALARY_CONFIG 计薪配置 =======================

    String HRM_SALARY_CONFIG_TYPE = "HRM 计薪配置";
    String HRM_SALARY_CONFIG_UPDATE_SUB_TYPE = "更新计薪配置";
    String HRM_SALARY_CONFIG_UPDATE_SUCCESS = "更新了计薪配置: {_DIFF{#updateReqVO}}";

    // ======================= HRM_SALARY_GROUP 薪资组 =======================

    String HRM_SALARY_GROUP_TYPE = "HRM 薪资组";
    String HRM_SALARY_GROUP_CREATE_SUB_TYPE = "创建薪资组";
    String HRM_SALARY_GROUP_CREATE_SUCCESS = "创建了薪资组【{{#salaryGroup.name}}】";
    String HRM_SALARY_GROUP_UPDATE_SUB_TYPE = "更新薪资组";
    String HRM_SALARY_GROUP_UPDATE_SUCCESS = "更新了薪资组【{{#updateReqVO.name}}】: {_DIFF{#updateReqVO}}";
    String HRM_SALARY_GROUP_DELETE_SUB_TYPE = "删除薪资组";
    String HRM_SALARY_GROUP_DELETE_SUCCESS = "删除了薪资组【{{#salaryGroup.name}}】";

    // ======================= HRM_SALARY_OPTION 薪资项 =======================

    String HRM_SALARY_OPTION_TYPE = "HRM 薪资项";
    String HRM_SALARY_OPTION_CREATE_SUB_TYPE = "创建薪资项";
    String HRM_SALARY_OPTION_CREATE_SUCCESS = "创建了薪资项【{{#salaryOption.name}}】";
    String HRM_SALARY_OPTION_UPDATE_ENABLED_SUB_TYPE = "更新薪资项启用状态";
    String HRM_SALARY_OPTION_UPDATE_ENABLED_SUCCESS =
            "将薪资项【{{#salaryOption.name}}】设置为【{{#enabled ? '启用' : '停用'}}】";
    String HRM_SALARY_OPTION_UPDATE_VISIBLE_SUB_TYPE = "更新薪资项显示状态";
    String HRM_SALARY_OPTION_UPDATE_VISIBLE_SUCCESS =
            "将薪资项【{{#salaryOption.name}}】设置为【{{#visible ? '显示' : '隐藏'}}】";
    String HRM_SALARY_OPTION_DELETE_SUB_TYPE = "删除薪资项";
    String HRM_SALARY_OPTION_DELETE_SUCCESS = "删除了薪资项【{{#salaryOption.name}}】";

    // ======================= HRM_SALARY_TAX_RULE 薪资计税规则 =======================

    String HRM_SALARY_TAX_RULE_TYPE = "HRM 薪资计税规则";
    String HRM_SALARY_TAX_RULE_CREATE_SUB_TYPE = "创建薪资计税规则";
    String HRM_SALARY_TAX_RULE_CREATE_SUCCESS = "创建了薪资计税规则【{{#salaryTaxRule.name}}】";
    String HRM_SALARY_TAX_RULE_UPDATE_SUB_TYPE = "更新薪资计税规则";
    String HRM_SALARY_TAX_RULE_UPDATE_SUCCESS =
            "更新了薪资计税规则【{{#salaryTaxRule.name}}】: {_DIFF{#updateReqVO}}";
    String HRM_SALARY_TAX_RULE_DELETE_SUB_TYPE = "删除薪资计税规则";
    String HRM_SALARY_TAX_RULE_DELETE_SUCCESS = "删除了薪资计税规则【{{#salaryTaxRule.name}}】";

    // ======================= HRM_SALARY_EMPLOYEE 员工薪资 =======================

    String HRM_SALARY_EMPLOYEE_TYPE = "HRM 员工薪资";
    String HRM_SALARY_EMPLOYEE_SET_SUB_TYPE = "员工定薪调薪";
    String HRM_SALARY_EMPLOYEE_SET_SUCCESS = "保存了员工【{{#employee.name}}】的定薪调薪记录";
    String HRM_SALARY_EMPLOYEE_BATCH_SET_SUB_TYPE = "批量调薪";
    String HRM_SALARY_EMPLOYEE_BATCH_SET_SUCCESS =
            "批量调薪完成，成功【{{#batchResult.successEmployeeIds.size()}}】人，"
                    + "失败【{{#batchResult.failureEmployeeReasons.size()}}】人";

    // ======================= HRM_SALARY_SLIP 工资条 =======================

    String HRM_SALARY_SLIP_TYPE = "HRM 工资条";
    String HRM_SALARY_SLIP_SEND_SUB_TYPE = "发放工资条";
    String HRM_SALARY_SLIP_SEND_SUCCESS = "发放了【{{#salarySlipSendRecord.year}}】年"
            + "【{{#salarySlipSendRecord.month}}】月工资条，共【{{#salarySlipSendRecord.sendEmployeeCount}}】人";
    String HRM_SALARY_SLIP_DELETE_SUB_TYPE = "删除工资条发放记录";
    String HRM_SALARY_SLIP_DELETE_SUCCESS = "删除了【{{#salarySlipSendRecord.year}}】年"
            + "【{{#salarySlipSendRecord.month}}】月工资条发放记录";

    // ======================= HRM_PERFORMANCE_PLAN 绩效计划 =======================

    String HRM_PERFORMANCE_PLAN_TYPE = "HRM 绩效计划";
    String HRM_PERFORMANCE_PLAN_CREATE_SUB_TYPE = "创建绩效计划";
    String HRM_PERFORMANCE_PLAN_CREATE_SUCCESS = "创建了绩效计划【{{#performancePlan.name}}】";
    String HRM_PERFORMANCE_PLAN_UPDATE_SUB_TYPE = "更新绩效计划";
    String HRM_PERFORMANCE_PLAN_UPDATE_SUCCESS = "更新了绩效计划【{{#performancePlan.name}}】";
    String HRM_PERFORMANCE_PLAN_DELETE_SUB_TYPE = "删除绩效计划";
    String HRM_PERFORMANCE_PLAN_DELETE_SUCCESS = "删除了绩效计划【{{#performancePlan.name}}】";
    String HRM_PERFORMANCE_PLAN_START_SUB_TYPE = "启动绩效计划";
    String HRM_PERFORMANCE_PLAN_START_SUCCESS = "启动了绩效计划【{{#performancePlan.name}}】";
    String HRM_PERFORMANCE_PLAN_OPEN_SCORING_SUB_TYPE = "开启绩效评分";
    String HRM_PERFORMANCE_PLAN_OPEN_SCORING_SUCCESS = "开启了绩效计划【{{#performancePlan.name}}】的评分";
    String HRM_PERFORMANCE_PLAN_INTERVIEW_SUB_TYPE = "发起绩效面谈";
    String HRM_PERFORMANCE_PLAN_INTERVIEW_SUCCESS = "发起了绩效计划【{{#performancePlan.name}}】的绩效面谈";
    String HRM_PERFORMANCE_PLAN_ARCHIVE_SUB_TYPE = "归档绩效计划";
    String HRM_PERFORMANCE_PLAN_ARCHIVE_SUCCESS = "归档了绩效计划【{{#performancePlan.name}}】";
    String HRM_PERFORMANCE_PLAN_TERMINATE_SUB_TYPE = "终止绩效计划";
    String HRM_PERFORMANCE_PLAN_TERMINATE_SUCCESS = "终止了绩效计划【{{#performancePlan.name}}】";

    // ======================= HRM_PERFORMANCE 绩效考核 =======================

    String HRM_PERFORMANCE_TYPE = "HRM 绩效考核";
    String HRM_PERFORMANCE_FILL_QUOTA_SUB_TYPE = "提交绩效指标";
    String HRM_PERFORMANCE_FILL_QUOTA_SUCCESS = "提交了绩效指标";
    String HRM_PERFORMANCE_SCORE_SUB_TYPE = "提交绩效评分";
    String HRM_PERFORMANCE_SCORE_SUCCESS = "提交了绩效评分";
    String HRM_PERFORMANCE_REJECT_SCORE_SUB_TYPE = "驳回绩效评分";
    String HRM_PERFORMANCE_REJECT_SCORE_SUCCESS = "驳回了上一绩效评分阶段，原因：【{{#reqVO.reason}}】";
    String HRM_PERFORMANCE_CONFIRM_TARGET_SUB_TYPE =
            "{{#reqVO.pass == 1 ? '确认绩效目标' : '驳回绩效目标'}}";
    String HRM_PERFORMANCE_CONFIRM_TARGET_SUCCESS =
            "{{#reqVO.pass == 1 ? '确认了绩效目标' : '驳回了绩效目标'}}"
                    + "{{#reqVO.comment != null && !#reqVO.comment.isEmpty() ? '，意见：【' + #reqVO.comment + '】' : ''}}";
    String HRM_PERFORMANCE_CONFIRM_RESULT_SUB_TYPE = "确认绩效结果";
    String HRM_PERFORMANCE_CONFIRM_RESULT_SUCCESS = "确认了绩效结果";
    String HRM_PERFORMANCE_APPEAL_SUB_TYPE = "提交绩效申诉";
    String HRM_PERFORMANCE_APPEAL_SUCCESS = "提交了绩效申诉，原因：【{{#reqVO.appealReason}}】";
    String HRM_PERFORMANCE_RESULT_AUDIT_SUB_TYPE =
            "{{#reqVO.pass == 1 ? '通过绩效结果审核' : '驳回绩效结果审核'}}";
    String HRM_PERFORMANCE_RESULT_AUDIT_SUCCESS =
            "{{#reqVO.pass == 1 ? '通过了绩效结果审核' : '驳回了绩效结果审核'}}"
                    + "{{#reqVO.comment != null && !#reqVO.comment.isEmpty() ? '，意见：【' + #reqVO.comment + '】' : ''}}";
    String HRM_PERFORMANCE_APPEAL_HANDLE_SUB_TYPE =
            "{{#reqVO.pass == 1 ? '通过绩效申诉' : '驳回绩效申诉'}}";
    String HRM_PERFORMANCE_APPEAL_HANDLE_SUCCESS =
            "{{#reqVO.pass == 1 ? '通过了绩效申诉' : '驳回了绩效申诉'}}"
                    + "{{#reqVO.comment != null && !#reqVO.comment.isEmpty() ? '，意见：【' + #reqVO.comment + '】' : ''}}";
    String HRM_PERFORMANCE_APPEAL_TIMEOUT_HANDLE_SUB_TYPE =
            "{{#appealPass ? '自动通过绩效申诉' : '自动驳回绩效申诉'}}";
    String HRM_PERFORMANCE_APPEAL_TIMEOUT_HANDLE_SUCCESS =
            "{{#appealPass ? '申诉确认超期，系统自动通过了绩效申诉' : '申诉确认超期，系统自动驳回了绩效申诉'}}";

}
