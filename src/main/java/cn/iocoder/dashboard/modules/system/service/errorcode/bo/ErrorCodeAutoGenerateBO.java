package cn.iocoder.dashboard.modules.system.service.errorcode.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 错误码自动生成 BO
 */
@Data
@Accessors(chain = true)
public class ErrorCodeAutoGenerateBO implements Serializable {

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
     * 错误码分组
     */
    @NotNull(message = "错误码分组不能为空")
    private String group;

}