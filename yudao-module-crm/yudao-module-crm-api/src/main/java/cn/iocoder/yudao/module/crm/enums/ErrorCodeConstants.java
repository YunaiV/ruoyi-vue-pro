package cn.iocoder.yudao.module.crm.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * CRM 错误码枚举类
 * <p>
 * crm 系统，使用 1-020-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 合同管理 1-020-000-000 ==========
    ErrorCode CONTRACT_NOT_EXISTS = new ErrorCode(1_020_000_000, "合同不存在");

    // TODO @wanwan：要单独一个分段噢
    ErrorCode CLUE_NOT_EXISTS = new ErrorCode(1_020_000_001, "线索不存在");

    // ========== 商机管理 1-020-001-000 ==========
    ErrorCode BUSINESS_NOT_EXISTS = new ErrorCode(1_020_001_000, "商机不存在");

}
