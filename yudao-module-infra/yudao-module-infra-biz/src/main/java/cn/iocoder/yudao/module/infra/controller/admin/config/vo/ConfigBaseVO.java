package cn.iocoder.yudao.module.infra.controller.admin.config.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 参数配置 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class ConfigBaseVO {

    @ApiModelProperty(value = "参数分组", required = true, example = "biz")
    @NotEmpty(message = "参数分组不能为空")
    @Size(max = 50, message = "参数名称不能超过50个字符")
    private String category;

    @ApiModelProperty(value = "参数名称", required = true, example = "数据库名")
    @NotBlank(message = "参数名称不能为空")
    @Size(max = 100, message = "参数名称不能超过100个字符")
    private String name;

    @ApiModelProperty(value = "参数键值", required = true, example = "1024")
    @NotBlank(message = "参数键值不能为空")
    @Size(max = 500, message = "参数键值长度不能超过500个字符")
    private String value;

    @ApiModelProperty(value = "是否敏感", required = true, example = "true")
    @NotNull(message = "是否敏感不能为空")
    private Boolean visible;

    @ApiModelProperty(value = "备注", example = "备注一下很帅气！")
    private String remark;

}
