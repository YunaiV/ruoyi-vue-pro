package cn.iocoder.dashboard.modules.system.service.errorcode.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
public class ErrorCodeCreateBO {

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
    private Integer type;
    /**
     * 错误码分组
     */
    private String group;
    /**
     * 错误码备注
     */
    private String memo;
}
