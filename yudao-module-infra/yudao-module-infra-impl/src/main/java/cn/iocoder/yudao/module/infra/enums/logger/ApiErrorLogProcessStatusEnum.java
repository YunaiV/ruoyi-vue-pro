package cn.iocoder.yudao.module.infra.enums.logger;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * API 异常数据的处理状态
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum ApiErrorLogProcessStatusEnum {

    INIT(0, "未处理"),
    DONE(1, "已处理"),
    IGNORE(2, "已忽略");

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 资源类型名
     */
    private final String name;

}
