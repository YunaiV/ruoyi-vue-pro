package cn.iocoder.yudao.module.system.controller.admin.errorcode.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

@ApiModel("管理后台 - 错误码 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErrorCodeRespVO extends ErrorCodeBaseVO {

    @ApiModelProperty(value = "错误码编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "错误码类型", required = true, example = "1", notes = "参见 ErrorCodeTypeEnum 枚举类")
    private Integer type;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
