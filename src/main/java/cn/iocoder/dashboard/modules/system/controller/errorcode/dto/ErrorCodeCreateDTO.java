package cn.iocoder.dashboard.modules.system.controller.errorcode.dto;

import cn.iocoder.dashboard.framework.validator.InEnum;
import cn.iocoder.dashboard.modules.system.enums.errorcode.ErrorCodeTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 错误码创建 DTO
 */
@Data
@Accessors(chain = true)
public class ErrorCodeCreateDTO implements Serializable {

    /**
     * 错误码编码
     */
    @NotNull(message = "错误码编码不能为空")
    private Integer code;
    /**
     * 错误码错误提示
     */
    @NotEmpty(message = "错误码错误提示不能为空")
    private String message;
    /**
     * 错误码类型
     */
    @NotNull(message = "错误码类型不能为空")
    @InEnum(value = ErrorCodeTypeEnum.class, message = "错误码类型必须是 {value}")
    private Integer type;
    /**
     * 错误码分组
     */
    @NotNull(message = "错误码分组不能为空")
    private String group;
    /**
     * 错误码备注
     */
    private String memo;

}
