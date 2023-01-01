package cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 粉丝标签 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 *
 * @author fengdan
 */
@Data
public class FansTagBaseVO {

    @NotBlank(message = "标签名不能为空")
    @ApiModelProperty(value = "标签名，UTF8编码")
    private String name;
}
