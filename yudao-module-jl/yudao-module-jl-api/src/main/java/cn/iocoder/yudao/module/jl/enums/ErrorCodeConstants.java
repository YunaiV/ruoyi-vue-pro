package cn.iocoder.yudao.module.jl.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

public interface ErrorCodeConstants {
    ErrorCode INSTITUTION_NOT_EXISTS = new ErrorCode(2001000001, "CRM 模块的机构/公司不存在");

    ErrorCode COMPETITOR_NOT_EXISTS = new ErrorCode(2002000001, "友商不存在");

    ErrorCode CUSTOMER_NOT_EXISTS = new ErrorCode(2003000001, "客户不存在");

    ErrorCode FOLLOWUP_NOT_EXISTS = new ErrorCode(2004000001, "销售跟进不存在");

    ErrorCode SALESLEAD_NOT_EXISTS = new ErrorCode(2005000001, "销售线索不存在");
}