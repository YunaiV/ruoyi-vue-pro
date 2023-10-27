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

    // ========== 线索管理 1-020-001-000 ==========
    ErrorCode CLUE_NOT_EXISTS = new ErrorCode(1_020_001_000, "线索不存在");

    // ========== 商机管理 1-020-002-000 ==========
    ErrorCode BUSINESS_NOT_EXISTS = new ErrorCode(1_020_002_000, "商机不存在");

    // ========== 联系人管理 1-020-003-000 ==========
    ErrorCode CONTACT_NOT_EXISTS = new ErrorCode(1_020_003_000, "联系人不存在");

    // ========== 回款管理 1-020-004-000 ==========
    ErrorCode RECEIVABLE_NOT_EXISTS = new ErrorCode(1_020_004_000, "回款管理不存在");

    // ========== 合同管理 1-020-005-000 ==========
    ErrorCode RECEIVABLE_PLAN_NOT_EXISTS = new ErrorCode(1_020_005_000, "回款计划不存在");

}
