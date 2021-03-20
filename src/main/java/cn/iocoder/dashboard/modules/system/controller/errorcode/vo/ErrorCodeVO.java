package cn.iocoder.dashboard.modules.system.controller.errorcode.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 错误码
 */
@ApiModel("错误码 VO")
@Data
public class ErrorCodeVO {

    @ApiModelProperty(value = "错误码编号", required = true, example = "1")
    private Integer id;
    @ApiModelProperty(value = "错误码编码", required = true, example = "10086")
    private Integer code;
    @ApiModelProperty(value = "错误码错误提示", required = true, example = "艿艿长的丑")
    private String message;
    @ApiModelProperty(value = "错误码类型", required = true, notes = "见 ErrorCodeTypeEnum 枚举", example = "1")
    private Integer type;
    @ApiModelProperty(value = "错误码分组", required = true, example = "user-service")
    private String group;
    @ApiModelProperty(value = "错误码备注", example = "我就是一个备注")
    private String memo;
    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;
    @ApiModelProperty(value = "更新时间", required = false)
    private Date updateTime;

}
