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
    ErrorCode STATISTICS_GET_USER_SUMMARY_FAIL = new ErrorCode(1006001000, "获取用户增减数据失败，原因：{}");
    ErrorCode STATISTICS_GET_USER_CUMULATE_FAIL = new ErrorCode(1006001001, "获得用户累计数据失败，原因：{}");
    ErrorCode STATISTICS_GET_UPSTREAM_MESSAGE_FAIL = new ErrorCode(1006001002, "获得消息发送概况数据失败，原因：{}");
    ErrorCode STATISTICS_GET_INTERFACE_SUMMARY_FAIL = new ErrorCode(1006001003, "获得接口分析数据失败，原因：{}");

    // ========== 公众号标签 1006002000============
    ErrorCode TAG_NOT_EXISTS = new ErrorCode(1006002000, "标签不存在");
    ErrorCode TAG_CREATE_FAIL = new ErrorCode(1006002001, "创建标签失败，原因：{}");
    ErrorCode TAG_UPDATE_FAIL = new ErrorCode(1006002001, "更新标签失败，原因：{}");
    ErrorCode TAG_DELETE_FAIL = new ErrorCode(1006002001, "删除标签失败，原因：{}");
    ErrorCode TAG_GET_FAIL = new ErrorCode(1006002001, "获得标签失败，原因：{}");

    // ========== 公众号粉丝 1006003000============
    ErrorCode USER_NOT_EXISTS = new ErrorCode(1006003000, "用户不存在");
    ErrorCode USER_UPDATE_TAG_FAIL = new ErrorCode(1006003001, "更新用户标签失败，原因：{}");

    // ========== 公众号素材 1006004000============
    ErrorCode MATERIAL_UPLOAD_FAIL = new ErrorCode(1006004000, "上传素材失败，原因：{}");

    // ========== 公众号素材 1006005000============
    ErrorCode MESSAGE_SEND_FAIL = new ErrorCode(1006005000, "发送消息失败，原因：{}");

    // TODO 要处理下
    ErrorCode MENU_NOT_EXISTS = new ErrorCode(1006001002, "菜单不存在");

}
