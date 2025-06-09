package com.somle.kingdee.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

public interface KingDeeErrorCodeConstants {
    // ========== 供应商 2-000-001-000 ==========
    ErrorCode SUPPLIER_LIST_BREAK = new ErrorCode(2_000_001_001, "获取供应商列表被中断,原因:{}");
    ErrorCode SUPPLIER_LIST_FAIL = new ErrorCode(2_000_001_002, "获取供应商列表数据失败,原因:{}");
    ErrorCode SUPPLIER_LIST_LOADING = new ErrorCode(2_000_001_003, "获取供应商列表数据中，请稍后重试");
}
