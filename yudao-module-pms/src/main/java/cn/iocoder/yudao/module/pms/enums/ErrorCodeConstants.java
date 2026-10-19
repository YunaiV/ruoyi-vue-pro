package cn.iocoder.yudao.module.pms.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * PMS 错误码常量
 *
 * PMS 系统，使用 1-053-000-000 段
 *
 * @author 芋道源码
 */
public interface ErrorCodeConstants {

    // ========== 项目 1-053-100-000 ==========
    ErrorCode PROJECT_NOT_EXISTS = new ErrorCode(1_053_100_000, "项目不存在");
    ErrorCode PROJECT_ACCESS_DENIED = new ErrorCode(1_053_100_001, "无权访问该项目");
    ErrorCode PROJECT_ADMIN_REQUIRED = new ErrorCode(1_053_100_002, "只有项目管理员可以执行该操作");
    ErrorCode PROJECT_STATUS_INVALID = new ErrorCode(1_053_100_004, "当前项目状态不允许执行该操作");
    ErrorCode PROJECT_WRITE_ACCESS_DENIED = new ErrorCode(1_053_100_005, "当前项目角色无编辑权限");
    ErrorCode PROJECT_ANNOUNCEMENT_NOT_EXISTS = new ErrorCode(1_053_100_006, "项目公告不存在");
    ErrorCode PROJECT_OWNER_REQUIRED = new ErrorCode(1_053_100_007, "只有项目拥有者可以执行该操作");

    // ========== 项目成员 1-053-105-000 ==========
    ErrorCode PROJECT_MEMBER_NOT_EXISTS = new ErrorCode(1_053_105_000, "项目成员不存在");
    ErrorCode PROJECT_MEMBER_CREATOR_CANNOT_REMOVE = new ErrorCode(1_053_105_001, "项目创建人不能移出项目");
    ErrorCode PROJECT_MEMBER_OWNER_LEVEL_CANNOT_ASSIGN = new ErrorCode(1_053_105_002,
            "项目拥有者级别不能直接分配");
    ErrorCode PROJECT_MEMBER_OWNER_CANNOT_EXIT = new ErrorCode(1_053_105_003, "项目拥有者不能退出项目");

    // ========== 项目分组 1-053-110-000 ==========
    ErrorCode PROJECT_GROUP_NOT_EXISTS = new ErrorCode(1_053_110_000, "项目分组不存在");
    ErrorCode PROJECT_GROUP_NAME_DUPLICATE = new ErrorCode(1_053_110_001, "项目分组名称已存在");
    ErrorCode PROJECT_GROUP_DEFAULT_CANNOT_MODIFY = new ErrorCode(1_053_110_002, "默认项目分组不允许修改");
    ErrorCode PROJECT_GROUP_DEFAULT_CANNOT_DELETE = new ErrorCode(1_053_110_003, "默认项目分组不允许删除");

    // ========== 项目模板 1-053-112-000 ==========
    ErrorCode PROJECT_TEMPLATE_NOT_EXISTS = new ErrorCode(1_053_112_000, "项目模板不存在");
    ErrorCode PROJECT_TEMPLATE_NAME_DUPLICATE = new ErrorCode(1_053_112_001, "同一项目类型下的模板名称已存在");
    ErrorCode PROJECT_TEMPLATE_CONFIG_INVALID = new ErrorCode(1_053_112_002, "项目模板配置无效：{}");

    // ========== 项目迭代 1-053-115-000 ==========
    ErrorCode ITERATION_NOT_EXISTS = new ErrorCode(1_053_115_000, "项目迭代不存在");
    ErrorCode ITERATION_STATUS_INVALID = new ErrorCode(1_053_115_002, "当前迭代状态不允许执行该操作");

    // ========== 工作项 1-053-120-000 ==========
    ErrorCode WORK_ITEM_NOT_EXISTS = new ErrorCode(1_053_120_000, "工作项不存在");
    ErrorCode WORK_ITEM_TYPE_INVALID = new ErrorCode(1_053_120_001, "当前项目不支持该工作项类型");
    ErrorCode WORK_ITEM_STATUS_NOT_EXISTS = new ErrorCode(1_053_120_002, "工作项状态不存在");
    ErrorCode WORK_ITEM_STATUS_INVALID = new ErrorCode(1_053_120_003, "工作项状态与项目或类型不匹配");
    ErrorCode WORK_ITEM_TIME_INVALID = new ErrorCode(1_053_120_004, "工作项开始时间必须早于截止时间");
    ErrorCode WORK_ITEM_ITERATION_INVALID = new ErrorCode(1_053_120_005, "所属迭代与工作项项目不匹配");
    ErrorCode WORK_ITEM_PARENT_INVALID = new ErrorCode(1_053_120_006, "父工作项与当前工作项不匹配");
    ErrorCode WORK_ITEM_RELATED_REQUIREMENT_INVALID = new ErrorCode(1_053_120_007, "关联需求与当前工作项不匹配");
    ErrorCode WORK_ITEM_DEFECT_TYPE_INVALID = new ErrorCode(1_053_120_008, "只有缺陷工作项可以设置缺陷类型");
    ErrorCode WORK_ITEM_SORT_INVALID = new ErrorCode(1_053_120_009, "工作项看板排序数据无效");
    ErrorCode WORK_ITEM_STATUS_NAME_DUPLICATE = new ErrorCode(1_053_120_010, "工作项状态名称已存在");
    ErrorCode WORK_ITEM_STATUS_DEFAULT_CANNOT_DELETE = new ErrorCode(1_053_120_011, "工作项初始状态不能删除");
    ErrorCode WORK_ITEM_STATUS_TRANSFER_INVALID = new ErrorCode(1_053_120_012, "工作项状态迁移目标无效");
    ErrorCode WORK_ITEM_STATUS_SORT_INVALID = new ErrorCode(1_053_120_013, "工作项状态排序数据无效");
    ErrorCode WORK_ITEM_WORK_LOG_NOT_EXISTS = new ErrorCode(1_053_120_014, "工作项工时记录不存在");
    ErrorCode WORK_ITEM_WORK_LOG_INVALID = new ErrorCode(1_053_120_015, "工时记录与工作项不匹配");
    ErrorCode WORK_ITEM_LABEL_NOT_EXISTS = new ErrorCode(1_053_120_016, "工作项标签不存在");
    ErrorCode WORK_ITEM_LABELS_INVALID = new ErrorCode(1_053_120_017, "工作项标签数据无效");
    ErrorCode WORK_ITEM_COMMENT_NOT_EXISTS = new ErrorCode(1_053_120_018, "工作项评论不存在");
    ErrorCode WORK_ITEM_COMMENT_REPLY_INVALID = new ErrorCode(1_053_120_019, "回复的评论与工作项不匹配");
    ErrorCode WORK_ITEM_COMMENT_ACCESS_DENIED = new ErrorCode(1_053_120_020, "只能修改或删除自己的评论");
    ErrorCode WORK_ITEM_LIFECYCLE_STATUS_INVALID = new ErrorCode(1_053_120_021, "工作项当前生命周期状态不支持该操作");
    ErrorCode WORK_ITEM_DELETE_STATUS_INVALID = new ErrorCode(1_053_120_022, "只有回收站中的工作项才能彻底删除");

    // ========== 知识库 1-053-130-000 ==========
    ErrorCode KNOWLEDGE_LIBRARY_NOT_EXISTS = new ErrorCode(1_053_130_000, "知识库不存在");
    ErrorCode KNOWLEDGE_LIBRARY_ACCESS_DENIED = new ErrorCode(1_053_130_001, "无权访问该知识库");
    ErrorCode KNOWLEDGE_LIBRARY_WRITE_ACCESS_DENIED = new ErrorCode(1_053_130_002, "无权编辑该知识库");
    ErrorCode KNOWLEDGE_LIBRARY_ADMIN_REQUIRED = new ErrorCode(1_053_130_003, "只有知识库管理员可以执行该操作");
    ErrorCode KNOWLEDGE_LIBRARY_CREATOR_CANNOT_REMOVE = new ErrorCode(1_053_130_004, "知识库创建人不能移出知识库");
    ErrorCode KNOWLEDGE_LIBRARY_MEMBERS_INVALID = new ErrorCode(1_053_130_005, "知识库成员数据无效");
    ErrorCode KNOWLEDGE_FOLDER_NOT_EXISTS = new ErrorCode(1_053_130_006, "知识库文件夹不存在");
    ErrorCode KNOWLEDGE_FOLDER_PARENT_INVALID = new ErrorCode(1_053_130_007, "父文件夹与知识库不匹配");
    ErrorCode KNOWLEDGE_FOLDER_MOVE_INVALID = new ErrorCode(1_053_130_008, "文件夹不能移动到自身或其子文件夹中");
    ErrorCode KNOWLEDGE_DOCUMENT_NOT_EXISTS = new ErrorCode(1_053_130_009, "知识库文档不存在");
    ErrorCode KNOWLEDGE_DOCUMENT_PARENT_INVALID = new ErrorCode(1_053_130_010, "父文档与知识库不匹配");
    ErrorCode KNOWLEDGE_DOCUMENT_FOLDER_INVALID = new ErrorCode(1_053_130_011, "文档文件夹与知识库不匹配");
    ErrorCode KNOWLEDGE_DOCUMENT_MOVE_INVALID = new ErrorCode(1_053_130_012, "文档不能移动到自身或其子文档中");
    ErrorCode KNOWLEDGE_DOCUMENT_STATUS_INVALID = new ErrorCode(1_053_130_013, "文档状态无效");
    ErrorCode KNOWLEDGE_DOCUMENT_TYPE_INVALID = new ErrorCode(1_053_130_014, "文档类型无效");
    ErrorCode KNOWLEDGE_DOCUMENT_MOVE_TARGET_INVALID = new ErrorCode(1_053_130_015, "文档移动目标只能选择文件夹或父文档之一");
    ErrorCode KNOWLEDGE_GROUP_NOT_EXISTS = new ErrorCode(1_053_130_020, "知识库分组不存在");
    ErrorCode KNOWLEDGE_GROUP_NAME_DUPLICATE = new ErrorCode(1_053_130_021, "知识库分组名称已存在");
    ErrorCode KNOWLEDGE_GROUP_DEFAULT_CANNOT_MODIFY = new ErrorCode(1_053_130_022, "默认知识库分组不允许修改");
    ErrorCode KNOWLEDGE_GROUP_DEFAULT_CANNOT_DELETE = new ErrorCode(1_053_130_023, "默认知识库分组不允许删除");
    ErrorCode KNOWLEDGE_GROUP_SORT_DUPLICATE = new ErrorCode(1_053_130_024, "知识库分组排序列表存在重复分组");
    ErrorCode KNOWLEDGE_DOCUMENT_LABEL_NOT_EXISTS = new ErrorCode(1_053_130_030, "知识库文档标签不存在");
    ErrorCode KNOWLEDGE_RECYCLE_RECORD_NOT_EXISTS = new ErrorCode(1_053_130_040, "知识库回收站记录不存在");
    ErrorCode KNOWLEDGE_RECYCLE_TYPE_INVALID = new ErrorCode(1_053_130_041, "知识库回收站对象类型无效");
    ErrorCode KNOWLEDGE_INTERACTION_OBJECT_INVALID = new ErrorCode(1_053_130_050, "知识对象不存在或类型不匹配");
    ErrorCode KNOWLEDGE_FAVORITE_DUPLICATE = new ErrorCode(1_053_130_051, "该知识对象已关注");
    ErrorCode KNOWLEDGE_DOCUMENT_SHARE_ALREADY_OPEN = new ErrorCode(1_053_130_060, "该文档已开启分享");
    ErrorCode KNOWLEDGE_DOCUMENT_SHARE_INVALID = new ErrorCode(1_053_130_061, "文档分享不存在或已失效");
    ErrorCode KNOWLEDGE_CONTENT_PERMISSION_NOT_EXISTS = new ErrorCode(1_053_130_070, "知识内容协作权限不存在");
    ErrorCode KNOWLEDGE_CONTENT_ACCESS_DENIED = new ErrorCode(1_053_130_071, "无权访问该知识内容");
    ErrorCode KNOWLEDGE_CONTENT_WRITE_ACCESS_DENIED = new ErrorCode(1_053_130_072, "无权编辑该知识内容");
    ErrorCode KNOWLEDGE_CONTENT_DELETE_ACCESS_DENIED = new ErrorCode(1_053_130_073, "无权删除该知识内容");
    ErrorCode KNOWLEDGE_CONTENT_PERMISSION_MEMBERS_INVALID = new ErrorCode(1_053_130_074, "知识内容协作者数据无效");
    ErrorCode KNOWLEDGE_DOCUMENT_COMMENT_NOT_EXISTS = new ErrorCode(1_053_130_080, "知识库文档评论不存在");
    ErrorCode KNOWLEDGE_DOCUMENT_COMMENT_REPLY_INVALID = new ErrorCode(1_053_130_081, "回复的评论与文档不匹配");
    ErrorCode KNOWLEDGE_DOCUMENT_COMMENT_ACCESS_DENIED = new ErrorCode(1_053_130_082, "只能删除自己的评论");
    ErrorCode KNOWLEDGE_DOCUMENT_COMMENT_TYPE_INVALID = new ErrorCode(1_053_130_083, "只有富文本文档支持评论");
    ErrorCode KNOWLEDGE_LIBRARY_TEMPLATE_NOT_EXISTS = new ErrorCode(1_053_130_084, "知识库模板不存在");
    ErrorCode KNOWLEDGE_LIBRARY_TEMPLATE_INVALID = new ErrorCode(1_053_130_085, "知识库模板配置无效");
    ErrorCode KNOWLEDGE_LIBRARY_DIRECT_MEMBER_NOT_EXISTS = new ErrorCode(1_053_130_086,
            "当前账号不是该知识库的直接成员");
    ErrorCode KNOWLEDGE_LIBRARY_TEMPLATE_NAME_DUPLICATE = new ErrorCode(1_053_130_087, "知识库模板名称已存在");

}
