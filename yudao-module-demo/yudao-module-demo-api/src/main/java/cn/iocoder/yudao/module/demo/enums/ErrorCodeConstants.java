package cn.iocoder.yudao.module.demo.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * 工作流 错误码枚举类
 *
 * 工作流系统，使用 1-009-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 区块链节点 TODO 补充编号 ==========
    ErrorCode ENDPOINT_NOT_EXISTS = new ErrorCode(1020001001, "区块链节点不存在");


    ErrorCode NET_NOT_EXISTS = new ErrorCode(1020002001, "区块链网络不存在");

}
