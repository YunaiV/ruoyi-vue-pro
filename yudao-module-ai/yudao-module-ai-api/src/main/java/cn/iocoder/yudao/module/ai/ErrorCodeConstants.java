package cn.iocoder.yudao.module.ai;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * System 错误码枚举类
 *
 * system 系统，使用 1-002-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 模块 ai 错误码区间 [1-022-000-000 ~ 1-023-000-000) ==========

    // TODO @fansili：1）类注释不太对；2）中英文之间，有个空格；例如说 AI 模型
    ErrorCode AI_MODULE_NOT_SUPPORTED = new ErrorCode(1_022_000_000, "AI模型暂不支持!");

    ErrorCode AI_CHAT_ROLE_NOT_EXISTENT = new ErrorCode(1_022_000_001, "AI Role 不存在!");;


    ErrorCode AI_CHAT_CONTINUE_CONVERSATION_ID_NOT_NULL = new ErrorCode(1_022_000_002, "chat 继续对话，对话id不能为空!");;



    ErrorCode AI_CHAT_CONTINUE_NOT_EXIST = new ErrorCode(1_022_000_020, "chat对话不存在!");
    ErrorCode AI_CHAT_CONVERSATION_NOT_YOURS = new ErrorCode(1_022_000_021, "这条chat对话不是你的!");

    ErrorCode AI_CHAT_ROLE_NOT_EXIST = new ErrorCode(1_022_000_040, "chatRole不存在!");



    ErrorCode AI_MODAL_NOT_EXIST = new ErrorCode(1_022_000_060, "ai模型不存在!");


}
