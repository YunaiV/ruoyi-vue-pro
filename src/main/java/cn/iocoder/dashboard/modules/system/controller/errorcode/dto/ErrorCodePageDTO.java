package cn.iocoder.dashboard.modules.system.controller.errorcode.dto;

import cn.iocoder.dashboard.common.pojo.PageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 错误码分页 DTO
 */
@ApiModel("错误码分页 DTO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ErrorCodePageDTO extends PageParam {

    @ApiModelProperty(value = "错误码编码", required = true)
    private Integer code;
    @ApiModelProperty(value = "错误码错误提示", required = true)
    private String message;
    @ApiModelProperty(value = "错误码分组", required = true)
    private String group;

}
