package cn.iocoder.yudao.module.oa.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * OA 错误码常量
 *
 * OA 系统，使用 1-054-000-000 段
 *
 * @author 芋道源码
 */
public interface ErrorCodeConstants {

    // ========== 流程申请 1-054-280-000 ==========
    ErrorCode APPLY_NOT_EXISTS = new ErrorCode(1_054_280_000, "申请不存在");
    ErrorCode APPLY_ACCESS_DENIED = new ErrorCode(1_054_280_001, "无权查看该申请");
    ErrorCode APPLY_STATUS_INVALID = new ErrorCode(1_054_280_004, "申请状态不允许该操作");
    ErrorCode LEAVE_APPLY_DAYS_EXCEEDED = new ErrorCode(1_054_280_005, "单次{}不能超过 {} 天");

    // ========== 出差管理 1-054-270-000 ==========
    ErrorCode TRAVEL_NOT_EXISTS = new ErrorCode(1_054_270_000, "出差单据不存在");
    ErrorCode TRAVEL_ACCESS_DENIED = new ErrorCode(1_054_270_001, "无权操作该出差单据");
    ErrorCode TRAVEL_STATUS_INVALID = new ErrorCode(1_054_270_002, "当前单据状态不允许该操作");
    ErrorCode TRAVEL_APPLY_ALREADY_REIMBURSED = new ErrorCode(1_054_270_010, "所选出差申请已报销，不能重复报销");
    ErrorCode TRAVEL_APPLY_NOT_APPROVED = new ErrorCode(1_054_270_009, "所选出差申请尚未审批通过");
    ErrorCode TRAVEL_NO_DUPLICATE = new ErrorCode(1_054_270_011, "出差单据编号已存在");

    // ========== 日程管理 1-054-100-000 ==========
    ErrorCode SCHEDULE_NOT_EXISTS = new ErrorCode(1_054_100_000, "日程不存在");
    ErrorCode SCHEDULE_ACCESS_DENIED = new ErrorCode(1_054_100_001, "无权访问该日程");

    // ========== 工作计划 1-054-110-000 ==========
    ErrorCode PLAN_NOT_EXISTS = new ErrorCode(1_054_110_000, "工作计划不存在");
    ErrorCode PLAN_ACCESS_DENIED = new ErrorCode(1_054_110_001, "无权操作该工作计划");

    // ========== 笔记管理 1-054-120-000 ==========
    ErrorCode NOTE_NOT_EXISTS = new ErrorCode(1_054_120_000, "笔记不存在");
    ErrorCode NOTE_ACCESS_DENIED = new ErrorCode(1_054_120_001, "无权访问该笔记");
    ErrorCode NOTE_CATEGORY_NOT_EXISTS = new ErrorCode(1_054_120_002, "笔记目录不存在");
    ErrorCode NOTE_CATEGORY_NAME_DUPLICATE = new ErrorCode(1_054_120_003, "笔记目录名称已存在");

    // ========== 通讯录 1-054-130-000 ==========
    ErrorCode CONTACT_NOT_EXISTS = new ErrorCode(1_054_130_000, "联系人不存在");
    ErrorCode CONTACT_ACCESS_DENIED = new ErrorCode(1_054_130_001, "无权访问该联系人");
    ErrorCode CONTACT_CATEGORY_NOT_EXISTS = new ErrorCode(1_054_130_002, "联系人分类不存在");
    ErrorCode CONTACT_CATEGORY_NAME_DUPLICATE = new ErrorCode(1_054_130_003, "联系人分类名称已存在");

    // ========== 讨论区 1-054-150-000 ==========
    ErrorCode DISCUSSION_NOT_EXISTS = new ErrorCode(1_054_150_000, "讨论不存在");
    ErrorCode DISCUSSION_ACCESS_DENIED = new ErrorCode(1_054_150_001, "只能修改或删除自己发布的讨论");
    ErrorCode DISCUSSION_REPLY_NOT_EXISTS = new ErrorCode(1_054_150_002, "讨论回复不存在");
    ErrorCode DISCUSSION_REPLY_ACCESS_DENIED = new ErrorCode(1_054_150_003, "只有讨论发布人或超级管理员可以删除回复");
    ErrorCode DISCUSSION_VOTE_INVALID = new ErrorCode(1_054_150_004, "投票配置无效");
    ErrorCode DISCUSSION_VOTE_EXPIRED = new ErrorCode(1_054_150_005, "投票未开始或已结束");
    ErrorCode DISCUSSION_VOTE_DUPLICATE = new ErrorCode(1_054_150_006, "已经参与过该投票");
    ErrorCode DISCUSSION_VOTE_OPTION_NOT_EXISTS = new ErrorCode(1_054_150_007, "投票选项不存在");
    ErrorCode DISCUSSION_LIKE_TARGET_INVALID = new ErrorCode(1_054_150_009, "只能点赞讨论或主回复");

    // ========== 考勤管理 1-054-160-000 ==========
    ErrorCode ATTENDANCE_NOT_EXISTS = new ErrorCode(1_054_160_000, "考勤记录不存在");
    ErrorCode ATTENDANCE_CLOCK_TIME_INVALID = new ErrorCode(1_054_160_001, "仅允许在每天 {} 之后、{} 之前打卡");
    ErrorCode ATTENDANCE_STATUS_INVALID = new ErrorCode(1_054_160_002, "考勤类型与状态不匹配");

    // ========== 任务管理 1-054-170-000 ==========
    ErrorCode TASK_NOT_EXISTS = new ErrorCode(1_054_170_000, "任务不存在");
    ErrorCode TASK_ACCESS_DENIED = new ErrorCode(1_054_170_001, "无权访问该任务");
    ErrorCode TASK_RECEIVER_NOT_EXISTS = new ErrorCode(1_054_170_003, "当前用户不是该任务的接收人");
    ErrorCode TASK_STATUS_TRANSITION_INVALID = new ErrorCode(1_054_170_004, "任务状态流转不合法");
    ErrorCode TASK_RECEIVED_DELETE_DENIED = new ErrorCode(1_054_170_006, "只能删除已取消的接收任务");

    // ========== 公告管理 1-054-180-000 ==========
    ErrorCode ANNOUNCEMENT_NOT_EXISTS = new ErrorCode(1_054_180_000, "公告不存在");
    ErrorCode ANNOUNCEMENT_ACCESS_DENIED = new ErrorCode(1_054_180_001, "无权操作该公告");
    ErrorCode ANNOUNCEMENT_RECEIVER_NOT_EXISTS = new ErrorCode(1_054_180_002, "当前用户不是该公告的接收人");
    ErrorCode ANNOUNCEMENT_ALREADY_FORWARDED = new ErrorCode(1_054_180_003, "公告已转发，请勿重复转发");
    ErrorCode ANNOUNCEMENT_UNREAD_DELETE_DENIED = new ErrorCode(1_054_180_004, "请先阅读公告，再移除收到的公告");

    // ========== 企业邮箱 1-054-190-000 ==========
    ErrorCode MAIL_PROVIDER_NOT_EXISTS = new ErrorCode(1_054_190_000, "邮箱服务配置不存在");
    ErrorCode MAIL_PROVIDER_DISABLED = new ErrorCode(1_054_190_001, "邮箱服务配置未启用");
    ErrorCode MAIL_PROVIDER_IN_USE = new ErrorCode(1_054_190_002, "邮箱服务配置已被账号引用");
    ErrorCode MAIL_ACCOUNT_NOT_EXISTS = new ErrorCode(1_054_190_003, "邮箱账号不存在或无权访问");
    ErrorCode MAIL_ACCOUNT_DISABLED = new ErrorCode(1_054_190_004, "邮箱账号未启用");
    ErrorCode MAIL_ACCOUNT_IDENTITY_IMMUTABLE = new ErrorCode(1_054_190_033, "邮箱绑定后不能修改地址、登录名或服务，请新增绑定");
    ErrorCode MAIL_ACCOUNT_PASSWORD_REQUIRED = new ErrorCode(1_054_190_005, "请输入邮箱密码或授权码");
    ErrorCode MAIL_MESSAGE_NOT_EXISTS = new ErrorCode(1_054_190_006, "邮件不存在或已移动，请先同步");
    ErrorCode MAIL_FOLDER_NOT_AVAILABLE = new ErrorCode(1_054_190_007, "文件夹不可用，请先同步");
    ErrorCode MAIL_SYNC_COUNT_EXCEEDED = new ErrorCode(1_054_190_008, "单文件夹超过 {} 封，请先缩小邮箱范围后同步");
    ErrorCode MAIL_SYNC_CHANGED = new ErrorCode(1_054_190_009, "同步期间邮箱发生变化，请重新同步");
    ErrorCode MAIL_UID_EXPIRED = new ErrorCode(1_054_190_010, "邮件标识已变化，请先同步");
    ErrorCode MAIL_PERMANENT_DELETE_UNSUPPORTED = new ErrorCode(1_054_190_011, "服务商不支持安全的单封彻底删除，请使用原邮箱客户端");
    ErrorCode MAIL_TRASH_NOT_EXISTS = new ErrorCode(1_054_190_012, "未识别到已删除文件夹，请使用原邮箱客户端");
    ErrorCode MAIL_MOVE_UNSUPPORTED = new ErrorCode(1_054_190_013, "服务商不支持安全移动，请使用原邮箱客户端操作");
    ErrorCode MAIL_RECIPIENT_REQUIRED = new ErrorCode(1_054_190_014, "请填写收件人");
    ErrorCode MAIL_SUBJECT_INVALID = new ErrorCode(1_054_190_015, "主题不能包含换行");
    ErrorCode MAIL_ADDRESS_INVALID = new ErrorCode(1_054_190_016, "邮件地址格式不正确，请检查收件人和抄送人");
    ErrorCode MAIL_NOT_DRAFT = new ErrorCode(1_054_190_017, "邮件不是草稿");
    ErrorCode MAIL_SYNC_FAILED = new ErrorCode(1_054_190_018, "邮箱同步失败，本地快照未更新，请检查连接后重试");
    ErrorCode MAIL_DETAIL_READ_FAILED = new ErrorCode(1_054_190_019, "邮件详情读取失败，请先同步或使用原邮箱客户端");
    ErrorCode MAIL_READ_STATUS_FAILED = new ErrorCode(1_054_190_020, "已读状态更新失败，请同步确认远端状态");
    ErrorCode MAIL_DELETE_UNCONFIRMED = new ErrorCode(1_054_190_021, "删除结果未确认，请先同步核实，勿重复操作");
    ErrorCode MAIL_DRAFT_FOLDER_NOT_EXISTS = new ErrorCode(1_054_190_022, "未识别到草稿箱，请先在原邮箱配置草稿文件夹");
    ErrorCode MAIL_DRAFT_REPLACE_UNSUPPORTED = new ErrorCode(1_054_190_023, "服务商不支持草稿安全替换，请使用原邮箱客户端");
    ErrorCode MAIL_DRAFT_SAVE_UNCONFIRMED = new ErrorCode(1_054_190_024, "草稿保存结果未确认，请同步草稿箱后核实，勿重复保存");
    ErrorCode MAIL_DRAFT_INDEX_NOT_FOUND = new ErrorCode(1_054_190_025, "草稿已保存，请同步后查看");
    ErrorCode MAIL_SOURCE_NOT_OWNED = new ErrorCode(1_054_190_026, "原邮件不属于当前邮箱");
    ErrorCode MAIL_SEND_UNCONFIRMED = new ErrorCode(1_054_190_027, "发送失败或结果未知，请先在原邮箱确认，勿直接重发");
    ErrorCode MAIL_DRAFT_NOT_OWNED = new ErrorCode(1_054_190_028, "只能修改当前账号的草稿");
    ErrorCode MAIL_SOURCE_READ_FAILED = new ErrorCode(1_054_190_029, "原邮件或附件读取失败，未执行发送");
    ErrorCode MAIL_ATTACHMENT_NOT_EXISTS = new ErrorCode(1_054_190_031, "邮件附件不存在");
    ErrorCode MAIL_ATTACHMENT_READ_FAILED = new ErrorCode(1_054_190_032, "邮件附件读取失败，请同步邮件后重试");
    ErrorCode MAIL_RESTORE_UNCONFIRMED = new ErrorCode(1_054_190_034, "恢复结果未确认，请先同步核实");
    ErrorCode MAIL_NOT_TRASH = new ErrorCode(1_054_190_035, "只能恢复已删除文件夹中的邮件");
    ErrorCode MAIL_INBOX_READ_FAILED = new ErrorCode(1_054_190_030, "收件箱读取失败，请检查邮箱连接配置或稍后重试");

    // ========== 办公用品 1-054-200-000 ==========
    ErrorCode SUPPLY_ITEM_PENDING_ISSUE = new ErrorCode(1_054_200_004, "物品尚有待发放明细，不能删除");
    ErrorCode SUPPLY_ITEM_NOT_RETURNED = new ErrorCode(1_054_200_002, "物品尚有未归还数量，不能删除");
    ErrorCode SUPPLY_ITEM_NOT_EXISTS = new ErrorCode(1_054_200_000, "办公用品不存在");
    ErrorCode SUPPLY_ITEM_NO_DUPLICATE = new ErrorCode(1_054_200_001, "用品编号已存在");
    ErrorCode SUPPLY_STOCK_INSUFFICIENT = new ErrorCode(1_054_200_003, "用品库存不足");
    ErrorCode SUPPLY_APPLY_NOT_EXISTS = new ErrorCode(1_054_200_005, "用品申请不存在");
    ErrorCode SUPPLY_APPLY_ACCESS_DENIED = new ErrorCode(1_054_200_006, "无权操作该用品申请");
    ErrorCode SUPPLY_APPLY_STATUS_INVALID = new ErrorCode(1_054_200_007, "当前申请状态不允许该操作");
    ErrorCode SUPPLY_APPLY_ITEM_INVALID = new ErrorCode(1_054_200_008, "用品已停用或重复");
    ErrorCode SUPPLY_APPLY_NO_DUPLICATE = new ErrorCode(1_054_200_009, "用品申请单号已存在");
    ErrorCode SUPPLY_APPLY_QTY_INVALID = new ErrorCode(1_054_200_010, "操作数量超过申请或待归还数量");
    ErrorCode SUPPLY_ISSUE_NOT_EXISTS = new ErrorCode(1_054_200_011, "用品发放或归还记录不存在");

    // ========== 印章管理 1-054-210-000 ==========
    ErrorCode SEAL_IN_USE = new ErrorCode(1_054_210_002, "印章已有业务单据引用，不能删除");
    ErrorCode SEAL_NOT_EXISTS = new ErrorCode(1_054_210_000, "印章不存在");
    ErrorCode SEAL_APPLY_NOT_EXISTS = new ErrorCode(1_054_210_013, "用印申请不存在");
    ErrorCode SEAL_APPLY_NOT_OWNER = new ErrorCode(1_054_210_014, "只能操作本人的用印申请");
    ErrorCode SEAL_APPLY_STATUS_INVALID = new ErrorCode(1_054_210_015, "用印申请状态不允许该操作");
    ErrorCode SEAL_APPLY_TIME_INVALID = new ErrorCode(1_054_210_016, "用印或归还时间不正确");
    ErrorCode SEAL_APPLY_TIME_CONFLICT = new ErrorCode(1_054_210_017, "该印章存在冲突预约或尚未归还");
    ErrorCode SEAL_APPLY_NO_DUPLICATE = new ErrorCode(1_054_210_021, "用印申请单号已存在");
    ErrorCode SEAL_NO_DUPLICATE = new ErrorCode(1_054_210_001, "印章编号已存在");

    // ========== 车辆管理 1-054-220-000 ==========
    ErrorCode VEHICLE_IN_USE = new ErrorCode(1_054_220_019, "车辆已有业务单据引用，不能删除");
    ErrorCode VEHICLE_NOT_EXISTS = new ErrorCode(1_054_220_000, "车辆不存在");
    ErrorCode VEHICLE_NO_DUPLICATE = new ErrorCode(1_054_220_001, "车牌号已存在");
    ErrorCode VEHICLE_NOT_ENABLED = new ErrorCode(1_054_220_002, "车辆非空闲状态，不能申请用车");
    ErrorCode VEHICLE_APPLY_NOT_EXISTS = new ErrorCode(1_054_220_003, "用车申请不存在");
    ErrorCode VEHICLE_APPLY_NOT_OWNER = new ErrorCode(1_054_220_004, "只能操作本人的用车申请");
    ErrorCode VEHICLE_APPLY_STATUS_INVALID = new ErrorCode(1_054_220_005, "用车申请状态不允许此操作");
    ErrorCode VEHICLE_APPLY_TIME_INVALID = new ErrorCode(1_054_220_006, "出车时间必须早于回车时间");
    ErrorCode VEHICLE_APPLY_TIME_CONFLICT = new ErrorCode(1_054_220_007, "该时段车辆已被预约，请调整用车时间");
    ErrorCode VEHICLE_RETURN_NOT_EXISTS = new ErrorCode(1_054_220_009, "还车申请不存在");
    ErrorCode VEHICLE_RETURN_NOT_OWNER = new ErrorCode(1_054_220_010, "只能操作本人的还车申请");
    ErrorCode VEHICLE_RETURN_STATUS_INVALID = new ErrorCode(1_054_220_011, "当前申请状态不允许还车操作");
    ErrorCode VEHICLE_RETURN_TIME_INVALID = new ErrorCode(1_054_220_014, "请填写实际出车时间，回车时间不能早于实际出车时间或晚于当前时间");

    ErrorCode VEHICLE_APPLY_NO_DUPLICATE = new ErrorCode(1_054_220_017, "用车申请单号已存在");

    ErrorCode VEHICLE_RETURN_NO_DUPLICATE = new ErrorCode(1_054_220_018, "还车申请单号已存在");

    // ========== 云盘文件 1-054-230-000 ==========
    ErrorCode FILE_NODE_NOT_EXISTS = new ErrorCode(1_054_230_000, "文件或目录不存在");
    ErrorCode FILE_NODE_NOT_AVAILABLE = new ErrorCode(1_054_230_001, "文件或所属目录已在回收站");
    ErrorCode FILE_NODE_PATH_INVALID = new ErrorCode(1_054_230_002, "文件目录结构异常或所属目录已删除");
    ErrorCode FILE_NODE_ACCESS_DENIED = new ErrorCode(1_054_230_003, "无权操作该文件或目录");
    ErrorCode FILE_NODE_NAME_DUPLICATE = new ErrorCode(1_054_230_004, "当前目录已存在同名文件或目录");
    ErrorCode FILE_NODE_MOVE_INVALID = new ErrorCode(1_054_230_005, "不能移动到自身或子目录");
    ErrorCode FILE_NODE_NOT_RECYCLED = new ErrorCode(1_054_230_007, "仅回收站内的文件可以恢复或彻底删除");
    ErrorCode FILE_STORAGE_NOT_ENOUGH = new ErrorCode(1_054_230_009, "云盘剩余容量不足，请清理文件后重试");
    ErrorCode FILE_PERMISSION_NOT_EXISTS = new ErrorCode(1_054_230_008, "共享权限不存在");

    // ========== 工作汇报 1-054-240-000 ==========
    ErrorCode WORK_REPORT_NOT_EXISTS = new ErrorCode(1_054_240_000, "工作汇报不存在");
    ErrorCode WORK_REPORT_ACCESS_DENIED = new ErrorCode(1_054_240_001, "无权访问该工作汇报");
    ErrorCode WORK_REPORT_STATUS_INVALID = new ErrorCode(1_054_240_002, "当前工作汇报状态不允许该操作");
    ErrorCode WORK_REPORT_NO_DUPLICATE = new ErrorCode(1_054_240_005, "工作汇报单号已存在");

    // ========== 会议室 1-054-250-000 ==========
    ErrorCode MEETING_ROOM_NOT_EXISTS = new ErrorCode(1_054_250_000, "会议室不存在");
    ErrorCode MEETING_ROOM_NOT_AVAILABLE = new ErrorCode(1_054_250_001, "会议室当前不可预定");
    ErrorCode MEETING_ROOM_SCOPE_INVALID = new ErrorCode(1_054_250_002, "指定成员不能为空");
    ErrorCode MEETING_ROOM_BOOKING_NOT_EXISTS = new ErrorCode(1_054_250_003, "会议室预定不存在");
    ErrorCode MEETING_ROOM_BOOKING_NOT_OWNER = new ErrorCode(1_054_250_004, "无权操作该会议室预定");
    ErrorCode MEETING_ROOM_BOOKING_STATUS_INVALID = new ErrorCode(1_054_250_005, "当前预定状态不允许该操作");
    ErrorCode MEETING_ROOM_BOOKING_TIME_INVALID = new ErrorCode(1_054_250_006, "会议时间不正确");
    ErrorCode MEETING_ROOM_BOOKING_TIME_CONFLICT = new ErrorCode(1_054_250_007, "会议室在该时段已被预定");
    ErrorCode MEETING_ROOM_HAS_BOOKING = new ErrorCode(1_054_250_009, "会议室存在有效预约，不能删除");
    ErrorCode MEETING_ROOM_BOOKING_NO_DUPLICATE = new ErrorCode(1_054_250_010, "会议室预定单号已存在");

    // ========== 公文管理 1-054-260-000 ==========
    ErrorCode OFFICIAL_DOC_TEMPLATE_NOT_EXISTS = new ErrorCode(1_054_260_000, "套红模板不存在");
    ErrorCode OFFICIAL_DOC_NOT_EXISTS = new ErrorCode(1_054_260_001, "公文不存在");
    ErrorCode OFFICIAL_DOC_ACCESS_DENIED = new ErrorCode(1_054_260_002, "无权操作该公文");
    ErrorCode OFFICIAL_DOC_STATUS_INVALID = new ErrorCode(1_054_260_003, "当前公文状态不允许该操作");
    ErrorCode OFFICIAL_DOC_PROCESS_MISMATCH = new ErrorCode(1_054_260_004, "公文与流程实例不匹配");
    ErrorCode OFFICIAL_DOC_TEMPLATE_DISABLED = new ErrorCode(1_054_260_005, "套红模板已停用");
    ErrorCode OFFICIAL_DOC_SEND_NO_DUPLICATE = new ErrorCode(1_054_260_007, "发文单据编号已存在");
    ErrorCode OFFICIAL_DOC_RECEIVE_NO_DUPLICATE = new ErrorCode(1_054_260_006, "收文单据编号已存在");
    ErrorCode OFFICIAL_DOC_TEMPLATE_IN_USE = new ErrorCode(1_054_260_008, "套红模板已有业务单据引用，不能删除");
    ErrorCode OFFICIAL_DOC_SEND_DOCUMENT_NO_DUPLICATE = new ErrorCode(1_054_260_009, "发文字号已存在");

}
