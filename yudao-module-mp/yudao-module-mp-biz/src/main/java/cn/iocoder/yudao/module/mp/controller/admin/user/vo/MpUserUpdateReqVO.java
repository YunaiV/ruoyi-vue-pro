package cn.iocoder.yudao.module.mp.controller.admin.user.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@ApiModel("管理后台 - 公众号粉丝更新 Request VO")
@Data
public class MpUserUpdateReqVO {

    @ApiModelProperty(value = "编号", required = true, example = "1024")
    @NotNull(message = "编号不能为空")
    private Long id;

    @ApiModelProperty(value = "昵称", example = "芋道")
    private String nickname;

    @ApiModelProperty(value = "备注", example = "你是一个芋头嘛")
    private String remark;

    @ApiModelProperty(value = "标签编号数组", example = "1,2,3")
    private List<Long> tagIds;

}
