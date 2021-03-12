package cn.iocoder.dashboard.modules.system.controller.errorcode.dto;

import cn.iocoder.dashboard.framework.validator.InEnum;
import cn.iocoder.dashboard.modules.system.enums.errorcode.ErrorCodeTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 错误码更新 DTO
 */
@Data
@Accessors(chain = true)
public class ErrorCodeUpdateDTO implements Serializable {

    /**
     * 错误码编号
     */
    @NotNull(message = "错误码编号不能为空")
    private Integer id;
    /**
     * 错误码编码
     */
    @NotNull(message = "错误码编码不能为空")
    private Integer code;
    /**
     * 错误码错误提示
     */
    private String message;
    /**
     * 错误码类型
     */
    @InEnum(value = ErrorCodeTypeEnum.class, message = "错误码类型必须是 {value}")
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
