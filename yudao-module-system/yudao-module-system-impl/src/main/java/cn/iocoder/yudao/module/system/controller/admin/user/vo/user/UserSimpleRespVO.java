package cn.iocoder.yudao.module.system.controller.admin.user.vo.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel("用户精简信息 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSimpleRespVO {

    @ApiModelProperty(value = "用户编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "用户昵称", required = true, example = "芋道")
    private String nickname;

}
