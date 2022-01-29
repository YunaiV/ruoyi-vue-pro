package cn.iocoder.yudao.module.system.controller.admin.dept.vo.post;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel("管理后台 - 岗位精简信息 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostSimpleRespVO {

    @ApiModelProperty(value = "岗位编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "岗位名称", required = true, example = "芋道")
    private String name;

}
