package cn.iocoder.yudao.module.mp.controller.admin.texttemplate.vo;

import lombok.*;
import io.swagger.annotations.*;

/**
 * 文本模板 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class WxTextTemplateBaseVO {

    @ApiModelProperty(value = "模板名字")
    private String tplName;

    @ApiModelProperty(value = "模板内容")
    private String content;

}
