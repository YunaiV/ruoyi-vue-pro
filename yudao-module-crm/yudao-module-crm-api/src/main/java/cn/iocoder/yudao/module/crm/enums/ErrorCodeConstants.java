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

    // TODO @lilleo：商机状态、商机类型，都单独错误码段

    ErrorCode BUSINESS_STATUS_TYPE_NOT_EXISTS = new ErrorCode(1_020_002_001, "商机状态类型不存在");
    ErrorCode BUSINESS_STATUS_NOT_EXISTS = new ErrorCode(1_020_002_002, "商机状态不存在");

    // ========== 联系人管理 1-020-003-000 ==========
    ErrorCode CONTACT_NOT_EXISTS = new ErrorCode(1_020_003_000, "联系人不存在");

    // ========== 回款管理 1-020-004-000 ==========
    ErrorCode RECEIVABLE_NOT_EXISTS = new ErrorCode(1_020_004_000, "回款管理不存在");

    // ========== 合同管理 1-020-005-000 ==========
    ErrorCode RECEIVABLE_PLAN_NOT_EXISTS = new ErrorCode(1_020_005_000, "回款计划不存在");

    // ========== 客户管理 1_020_006_000 ==========
    ErrorCode CUSTOMER_NOT_EXISTS = new ErrorCode(1_020_006_000, "客户不存在");
    ErrorCode CUSTOMER_OWNER_EXISTS = new ErrorCode(1_020_006_001, "客户已存在所属负责人");
    ErrorCode CUSTOMER_LOCKED = new ErrorCode(1_020_006_002, "客户状态已锁定");
    ErrorCode CUSTOMER_ALREADY_DEAL = new ErrorCode(1_020_006_003, "客户已交易");
    // TODO @wanwan：这 2 个单独配置段噢
    ErrorCode CUSTOMER_POOL_CONFIG_ERROR = new ErrorCode(1_020_006_001, "客户公海规则设置不正确");
    ErrorCode CUSTOMER_LIMIT_CONFIG_NOT_EXISTS = new ErrorCode(1_020_006_002, "客户限制配置不存在");

    // ========== 权限管理 1_020_007_000 ==========
    ErrorCode CRM_PERMISSION_NOT_EXISTS = new ErrorCode(1_020_007_000, "数据权限不存在");
    ErrorCode CRM_PERMISSION_DENIED = new ErrorCode(1_020_007_001, "{}操作失败，原因：没有权限");
    ErrorCode CRM_PERMISSION_MODEL_NOT_EXISTS = new ErrorCode(1_020_007_002, "{}不存在");
    ErrorCode CRM_PERMISSION_MODEL_TRANSFER_FAIL_OWNER_USER_EXISTS = new ErrorCode(1_020_007_003, "{}操作失败，原因：转移对象已经是该负责人");
    ErrorCode CRM_PERMISSION_DELETE_FAIL = new ErrorCode(1_020_007_004, "删除数据权限失败，原因：参数异常");
    ErrorCode CRM_PERMISSION_DELETE_FAIL_EXIST_OWNER = new ErrorCode(1_020_007_005, "删除数据权限失败，原因：存在负责人");
    ErrorCode CRM_PERMISSION_DELETE_DENIED = new ErrorCode(1_020_007_006, "删除数据权限失败，原因：没有权限");
    ErrorCode CRM_PERMISSION_DELETE_SELF_PERMISSION_FAIL_EXIST_OWNER = new ErrorCode(1_020_007_007, "删除数据权限失败，原因：不能删除负责人");

    // ========== 产品 1_020_008_000 ==========
    ErrorCode PRODUCT_NOT_EXISTS = new ErrorCode(1_020_008_000, "产品不存在");
    ErrorCode PRODUCT_NO_EXISTS = new ErrorCode(1_020_008_001, "产品编号已存在");

    // ========== 产品分类 1_020_009_000 ==========
    ErrorCode PRODUCT_CATEGORY_NOT_EXISTS = new ErrorCode(1_020_009_000, "产品分类不存在");

}
