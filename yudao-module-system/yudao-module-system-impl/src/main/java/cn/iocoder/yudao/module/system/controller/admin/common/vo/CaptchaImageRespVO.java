package cn.iocoder.yudao.module.system.controller.admin.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel("管理后台 - 验证码图片 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaImageRespVO {

    @ApiModelProperty(value = "uuid", required = true, example = "1b3b7d00-83a8-4638-9e37-d67011855968",
            notes = "通过该 uuid 作为该验证码的标识")
    private String uuid;

    @ApiModelProperty(value = "图片", required = true, notes = "验证码的图片内容，使用 Base64 编码")
    private String img;

}
