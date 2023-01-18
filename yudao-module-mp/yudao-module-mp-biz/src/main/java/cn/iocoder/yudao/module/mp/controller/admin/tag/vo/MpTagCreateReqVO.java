package cn.iocoder.yudao.module.mp.controller.admin.tag.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@ApiModel("管理后台 - 公众号标签创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MpTagCreateReqVO extends MpTagBaseVO {

    @ApiModelProperty(value = "公众号账号的编号", required = true, example = "2048")
    @NotNull(message = "公众号账号的编号不能为空")
    private Long accountId;

}
