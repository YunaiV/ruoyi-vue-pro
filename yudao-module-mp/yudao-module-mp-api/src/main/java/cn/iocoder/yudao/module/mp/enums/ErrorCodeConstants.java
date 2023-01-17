package cn.iocoder.yudao.module.mp.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Mp 错误码枚举类
 *
 * mp 系统，使用 1-006-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 公众号账号 1006000000============
    ErrorCode ACCOUNT_NOT_EXISTS = new ErrorCode(1006000000, "公众号账号不存在");
    ErrorCode ACCOUNT_GENERATE_QR_CODE_FAIL = new ErrorCode(1006000001, "生成公众号二维码失败，原因：{}");
    ErrorCode ACCOUNT_CLEAR_QUOTA_FAIL = new ErrorCode(1006000001, "清空公众号的 API 配额失败，原因：{}");

    // ========== 公众号统计 1006001000============
    ErrorCode STATISTICS_GET_USER_SUMMARY_FAIL = new ErrorCode(1006001000, "获取粉丝增减数据失败，原因：{}");
    ErrorCode STATISTICS_GET_USER_CUMULATE_FAIL = new ErrorCode(1006001001, "获得粉丝累计数据失败，原因：{}");
    ErrorCode STATISTICS_GET_UPSTREAM_MESSAGE_FAIL = new ErrorCode(1006001002, "获得消息发送概况数据失败，原因：{}");
    ErrorCode STATISTICS_GET_INTERFACE_SUMMARY_FAIL = new ErrorCode(1006001003, "获得接口分析数据失败，原因：{}");

    // ========== 公众号标签 1006002000============
    ErrorCode TAG_NOT_EXISTS = new ErrorCode(1006002000, "标签不存在");
    ErrorCode TAG_CREATE_FAIL = new ErrorCode(1006002001, "创建标签失败，原因：{}");
    ErrorCode TAG_UPDATE_FAIL = new ErrorCode(1006002001, "更新标签失败，原因：{}");
    ErrorCode TAG_DELETE_FAIL = new ErrorCode(1006002001, "删除标签失败，原因：{}");
    ErrorCode TAG_GET_FAIL = new ErrorCode(1006002001, "获得标签失败，原因：{}");

    // ========== 公众号粉丝 1006003000============
    ErrorCode USER_NOT_EXISTS = new ErrorCode(1006003000, "粉丝不存在");
    ErrorCode USER_UPDATE_TAG_FAIL = new ErrorCode(1006003001, "更新粉丝标签失败，原因：{}");

    // ========== 公众号素材 1006004000============
    ErrorCode MATERIAL_NOT_EXISTS = new ErrorCode(1006004000, "素材不存在");
    ErrorCode MATERIAL_UPLOAD_FAIL = new ErrorCode(1006004001, "上传素材失败，原因：{}");
    ErrorCode MATERIAL_IMAGE_UPLOAD_FAIL = new ErrorCode(1006004002, "上传图片失败，原因：{}");
    ErrorCode MATERIAL_DELETE_FAIL = new ErrorCode(1006004003, "删除素材失败，原因：{}");

    // ========== 公众号消息 1006005000============
    ErrorCode MESSAGE_SEND_FAIL = new ErrorCode(1006005000, "发送消息失败，原因：{}");

    // ========== 公众号发布能力 1006006000============
    ErrorCode FREE_PUBLISH_LIST_FAIL = new ErrorCode(1006006000, "获得已成功发布列表失败，原因：{}");
    ErrorCode FREE_PUBLISH_SUBMIT_FAIL = new ErrorCode(1006006001, "提交发布失败，原因：{}");
    ErrorCode FREE_PUBLISH_DELETE_FAIL = new ErrorCode(1006006001, "删除发布失败，原因：{}");

    // ========== 公众号草稿 1006007000============
    ErrorCode DRAFT_LIST_FAIL = new ErrorCode(1006007000, "获得草稿列表失败，原因：{}");
    ErrorCode DRAFT_CREATE_FAIL = new ErrorCode(1006007001, "创建草稿失败，原因：{}");
    ErrorCode DRAFT_UPDATE_FAIL = new ErrorCode(1006007002, "更新草稿失败，原因：{}");
    ErrorCode DRAFT_DELETE_FAIL = new ErrorCode(1006007002, "删除草稿失败，原因：{}");

    // ========== 公众号菜单 1006008000============
    ErrorCode MENU_SAVE_FAIL = new ErrorCode(1006008000, "创建菜单失败，原因：{}");
    ErrorCode MENU_DELETE_FAIL = new ErrorCode(1006008001, "删除菜单失败，原因：{}");

    // ========== 公众号自动回复 1006009000============
    ErrorCode AUTO_REPLY_NOT_EXISTS = new ErrorCode(1006009000, "自动回复不存在");
    ErrorCode AUTO_REPLY_ADD_SUBSCRIBE_FAIL_EXISTS = new ErrorCode(1006009001, "操作失败，原因：已存在关注时的回复");
    ErrorCode AUTO_REPLY_ADD_MESSAGE_FAIL_EXISTS = new ErrorCode(1006009002, "操作失败，原因：已存在该消息类型的回复");
    ErrorCode AUTO_REPLY_ADD_KEYWORD_FAIL_EXISTS = new ErrorCode(1006009003, "操作失败，原因：已关在该关键字的回复");

}
