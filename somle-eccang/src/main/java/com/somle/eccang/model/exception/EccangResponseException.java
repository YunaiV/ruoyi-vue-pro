package com.somle.eccang.model.exception;


import com.somle.eccang.model.EccangResponse;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义异常：用于处理 Eccang 特定的错误（如错误代码 300 和 10001）
 */
@Data
public class EccangResponseException extends RuntimeException {

    // 用于标识错误的额外信息
    private final List<EccangResponse.EccangError> eccangError;

    // 构造函数
    public EccangResponseException(List<EccangResponse.EccangError> eccangError) {
        super("Eccang error occurred, ErrorCode: " + eccangError.stream()
            .map(EccangResponse.EccangError::getErrorCode)  // 提取 ErrorCode
            .collect(Collectors.toSet())
            + ", Message: " + eccangError.stream()
            .map(EccangResponse.EccangError::getErrorMsg)  // 提取 ErrorMsg
            .collect(Collectors.toSet()));
        this.eccangError = eccangError;
    }

}
