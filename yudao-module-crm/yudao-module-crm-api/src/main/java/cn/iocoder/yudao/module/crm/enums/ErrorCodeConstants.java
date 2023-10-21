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

    // ========== 商机管理 1-020-001-000 ==========
    ErrorCode BUSINESS_NOT_EXISTS = new ErrorCode(1_020_001_000, "商机不存在");

    // TODO @liuhongfeng：错误码分段；
    ErrorCode RECEIVABLE_NOT_EXISTS = new ErrorCode(1_030_000_001, "回款管理不存在");

    ErrorCode RECEIVABLE_PLAN_NOT_EXISTS = new ErrorCode(1_040_000_001, "回款计划不存在");

}
