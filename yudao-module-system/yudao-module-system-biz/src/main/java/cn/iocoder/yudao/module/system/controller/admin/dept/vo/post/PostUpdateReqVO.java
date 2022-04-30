package cn.iocoder.yudao.module.system.controller.admin.dept.vo.post;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@ApiModel("管理后台 - 岗位更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class PostUpdateReqVO extends PostBaseVO {

    @ApiModelProperty(value = "岗位编号", required = true, example = "1024")
    @NotNull(message = "岗位编号不能为空")
    private Long id;

}
