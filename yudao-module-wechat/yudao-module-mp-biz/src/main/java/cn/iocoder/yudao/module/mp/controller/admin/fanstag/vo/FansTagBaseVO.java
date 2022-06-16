package cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 粉丝标签 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 *
 * @author fengdan
 */
@Data
public class FansTagBaseVO {

    @ApiModelProperty(value = "标签名，UTF8编码.")
    private String name;

    @ApiModelProperty(value = "此标签下粉丝数")
    private Integer count;
}
