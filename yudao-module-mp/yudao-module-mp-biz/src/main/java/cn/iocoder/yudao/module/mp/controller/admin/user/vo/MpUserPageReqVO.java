package cn.iocoder.yudao.module.mp.controller.admin.user.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@ApiModel("管理后台 - 公众号粉丝分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MpUserPageReqVO extends PageParam {

    @ApiModelProperty(value = "公众号账号的编号", required = true, example = "2048")
    @NotNull(message = "公众号账号的编号不能为空")
    private Long accountId;

    @ApiModelProperty(value = "公众号用户标识", example = "o6_bmjrPTlm6_2sgVt7hMZOPfL2M", notes = "模糊匹配")
    private String openid;

    @ApiModelProperty(value = "公众号用户昵称", example = "芋艿", notes = "模糊匹配")
    private String nickname;

}
