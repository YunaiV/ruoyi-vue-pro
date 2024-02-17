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
    ErrorCode CONTRACT_UPDATE_FAIL_EDITING_PROHIBITED = new ErrorCode(1_020_000_001, "更新合同失败，原因：禁止编辑");
    ErrorCode CONTRACT_SUBMIT_FAIL_NOT_DRAFT = new ErrorCode(1_020_000_002, "合同提交审核失败，原因：合同没处在未提交状态");

    // ========== 线索管理 1-020-001-000 ==========
    ErrorCode CLUE_NOT_EXISTS = new ErrorCode(1_020_001_000, "线索不存在");
    ErrorCode CLUE_ANY_CLUE_NOT_EXISTS = new ErrorCode(1_020_001_001, "线索【{}】不存在");
    ErrorCode CLUE_ANY_CLUE_ALREADY_TRANSLATED = new ErrorCode(1_020_001_002, "线索【{}】已经转化过了，请勿重复转化");

    // ========== 商机管理 1-020-002-000 ==========
    ErrorCode BUSINESS_NOT_EXISTS = new ErrorCode(1_020_002_000, "商机不存在");
    ErrorCode BUSINESS_CONTRACT_EXISTS = new ErrorCode(1_020_002_001, "商机已关联合同，不能删除");

    // TODO @lilleo：商机状态、商机类型，都单独错误码段


    // ========== 联系人管理 1-020-003-000 ==========
    ErrorCode CONTACT_NOT_EXISTS = new ErrorCode(1_020_003_000, "联系人不存在");
    ErrorCode CONTACT_BUSINESS_LINK_NOT_EXISTS = new ErrorCode(1_020_003_001, "联系人商机关联不存在");
    ErrorCode CONTACT_DELETE_FAIL_CONTRACT_LINK_EXISTS = new ErrorCode(1_020_003_002, "联系人已关联合同，不能删除");

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
    ErrorCode CUSTOMER_LOCK_FAIL_IS_LOCK = new ErrorCode(1_020_006_007, "锁定客户失败，它已经处于锁定状态");
    ErrorCode CUSTOMER_UNLOCK_FAIL_IS_UNLOCK = new ErrorCode(1_020_006_008, "解锁客户失败，它已经处于未锁定状态");
    ErrorCode CUSTOMER_LOCK_EXCEED_LIMIT = new ErrorCode(1_020_006_009, "锁定客户失败，超出锁定规则上限");
    ErrorCode CUSTOMER_OWNER_EXCEED_LIMIT = new ErrorCode(1_020_006_010, "操作失败，超出客户数拥有上限");
    ErrorCode CUSTOMER_DELETE_FAIL_HAVE_REFERENCE = new ErrorCode(1_020_006_011, "删除客户失败，有关联{}");
    ErrorCode CUSTOMER_IMPORT_LIST_IS_EMPTY = new ErrorCode(1_020_006_012, "导入客户数据不能为空！");
    ErrorCode CUSTOMER_CREATE_NAME_NOT_NULL = new ErrorCode(1_020_006_013, "客户名称不能为空！");
    ErrorCode CUSTOMER_NAME_EXISTS = new ErrorCode(1_020_006_014, "已存在名为【{}】的客户！");

    // ========== 权限管理 1_020_007_000 ==========
    ErrorCode CRM_PERMISSION_NOT_EXISTS = new ErrorCode(1_020_007_000, "数据权限不存在");
    ErrorCode CRM_PERMISSION_DENIED = new ErrorCode(1_020_007_001, "{}操作失败，原因：没有权限");
    ErrorCode CRM_PERMISSION_MODEL_NOT_EXISTS = new ErrorCode(1_020_007_002, "{}不存在");
    ErrorCode CRM_PERMISSION_MODEL_TRANSFER_FAIL_OWNER_USER_EXISTS = new ErrorCode(1_020_007_003, "{}操作失败，原因：转移对象已经是该负责人");
    ErrorCode CRM_PERMISSION_DELETE_FAIL = new ErrorCode(1_020_007_004, "删除数据权限失败，原因：批量删除权限的时候，只能属于同一个 bizId 下");
    ErrorCode CRM_PERMISSION_DELETE_FAIL_EXIST_OWNER = new ErrorCode(1_020_007_005, "删除数据权限失败，原因：存在负责人");
    ErrorCode CRM_PERMISSION_DELETE_DENIED = new ErrorCode(1_020_007_006, "删除数据权限失败，原因：没有权限");
    ErrorCode CRM_PERMISSION_DELETE_SELF_PERMISSION_FAIL_EXIST_OWNER = new ErrorCode(1_020_007_007, "删除数据权限失败，原因：不能删除负责人");
    ErrorCode CRM_PERMISSION_CREATE_FAIL = new ErrorCode(1_020_007_008, "创建数据权限失败，原因：所加用户已有权限");

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
    ErrorCode CUSTOMER_POOL_CONFIG_NOT_EXISTS_OR_DISABLED = new ErrorCode(1_020_012_000, "客户公海配置不存在或未启用");
    ErrorCode CUSTOMER_LIMIT_CONFIG_NOT_EXISTS = new ErrorCode(1_020_012_001, "客户限制配置不存在");

    // ========== 跟进记录 1_020_013_000 ==========
    ErrorCode FOLLOW_UP_RECORD_NOT_EXISTS = new ErrorCode(1_020_013_000, "跟进记录不存在");
    ErrorCode FOLLOW_UP_RECORD_DELETE_DENIED = new ErrorCode(1_020_013_001, "删除跟进记录失败，原因：没有权限");

    // ========== 待办消息 1_020_014_000 ==========

}
