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


    // ========== 联系人管理 1-020-003-000 ==========
    ErrorCode CONTACT_NOT_EXISTS = new ErrorCode(1_020_003_000, "联系人不存在");
    ErrorCode CONTACT_BUSINESS_LINK_NOT_EXISTS = new ErrorCode( 1_020_003_001, "联系人商机关联不存在");

    // ========== 回款 1-020-004-000 ==========
    ErrorCode RECEIVABLE_NOT_EXISTS = new ErrorCode(1_020_004_000, "回款不存在");

    // ========== 合同管理 1-020-005-000 ==========
    ErrorCode RECEIVABLE_PLAN_NOT_EXISTS = new ErrorCode(1_020_005_000, "回款计划不存在");

    // ========== 客户管理 1_020_006_000 ==========
    ErrorCode CUSTOMER_NOT_EXISTS = new ErrorCode(1_020_006_000, "客户不存在");
    ErrorCode CUSTOMER_OWNER_EXISTS = new ErrorCode(1_020_006_001, "客户【{}】已存在所属负责人");
    ErrorCode CUSTOMER_LOCKED = new ErrorCode(1_020_006_002, "客户【{}】状态已锁定");
    ErrorCode CUSTOMER_ALREADY_DEAL = new ErrorCode(1_020_006_003, "客户已交易");
    ErrorCode CUSTOMER_IN_POOL = new ErrorCode(1_020_006_004, "客户【{}】放入公海失败，原因：已经是公海客户");
    ErrorCode CUSTOMER_LOCKED_PUT_POOL_FAIL = new ErrorCode(1_020_006_005, "客户【{}】放入公海失败，原因：客户已锁定");
    ErrorCode CUSTOMER_UPDATE_OWNER_USER_FAIL = new ErrorCode(1_020_006_006, "更新客户【{}】负责人失败, 原因：系统异常");

    // ========== 权限管理 1_020_007_000 ==========
    ErrorCode CRM_PERMISSION_NOT_EXISTS = new ErrorCode(1_020_007_000, "数据权限不存在");
    ErrorCode CRM_PERMISSION_DENIED = new ErrorCode(1_020_007_001, "{}操作失败，原因：没有权限");
    ErrorCode CRM_PERMISSION_MODEL_NOT_EXISTS = new ErrorCode(1_020_007_002, "{}不存在");
    ErrorCode CRM_PERMISSION_MODEL_TRANSFER_FAIL_OWNER_USER_EXISTS = new ErrorCode(1_020_007_003, "{}操作失败，原因：转移对象已经是该负责人");
    ErrorCode CRM_PERMISSION_DELETE_FAIL = new ErrorCode(1_020_007_004, "删除数据权限失败，原因：批量删除权限的时候，只能属于同一个 bizId 下");
    ErrorCode CRM_PERMISSION_DELETE_FAIL_EXIST_OWNER = new ErrorCode(1_020_007_005, "删除数据权限失败，原因：存在负责人");
    ErrorCode CRM_PERMISSION_DELETE_DENIED = new ErrorCode(1_020_007_006, "删除数据权限失败，原因：没有权限");
    ErrorCode CRM_PERMISSION_DELETE_SELF_PERMISSION_FAIL_EXIST_OWNER = new ErrorCode(1_020_007_007, "删除数据权限失败，原因：不能删除负责人");

    // ========== 产品 1_020_008_000 ==========
    ErrorCode PRODUCT_NOT_EXISTS = new ErrorCode(1_020_008_000, "产品不存在");
    ErrorCode PRODUCT_NO_EXISTS = new ErrorCode(1_020_008_001, "产品编号已存在");

    // ========== 产品分类 1_020_009_000 ==========
    ErrorCode PRODUCT_CATEGORY_NOT_EXISTS = new ErrorCode(1_020_009_000, "产品分类不存在");
    ErrorCode PRODUCT_CATEGORY_EXISTS = new ErrorCode(1_020_009_001, "产品分类已存在");
    ErrorCode PRODUCT_CATEGORY_USED = new ErrorCode(1_020_009_002, "产品分类已关联产品");
    ErrorCode PRODUCT_CATEGORY_PARENT_NOT_EXISTS = new ErrorCode(1_020_009_003, "父分类不存在");
    ErrorCode PRODUCT_CATEGORY_PARENT_NOT_FIRST_LEVEL = new ErrorCode(1_020_009_004, "父分类不能是二级分类");
    ErrorCode product_CATEGORY_EXISTS_CHILDREN = new ErrorCode(1_020_009_005, "存在子分类，无法删除");

    // ========== 商机状态类型 1_020_010_000 ==========
    ErrorCode BUSINESS_STATUS_TYPE_NOT_EXISTS = new ErrorCode(1_020_010_000, "商机状态类型不存在");
    ErrorCode BUSINESS_STATUS_TYPE_NAME_EXISTS = new ErrorCode(1_020_010_001, "商机状态类型名称已存在");

    // ========== 商机状态 1_020_011_000 ==========
    ErrorCode BUSINESS_STATUS_NOT_EXISTS = new ErrorCode(1_020_011_000, "商机状态不存在");

    // ========== 客户公海规则设置 1_020_012_000 ==========
    ErrorCode CUSTOMER_LIMIT_CONFIG_NOT_EXISTS = new ErrorCode(1_020_012_000, "客户限制配置不存在");

}
