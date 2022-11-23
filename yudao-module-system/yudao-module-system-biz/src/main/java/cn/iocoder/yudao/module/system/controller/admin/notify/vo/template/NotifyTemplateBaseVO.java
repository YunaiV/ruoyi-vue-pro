package cn.iocoder.yudao.module.system.controller.admin.notify.vo.template;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
* 站内信模版 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class NotifyTemplateBaseVO {

    @ApiModelProperty(value = "模版编码", required = true)
    @NotNull(message = "模版编码不能为空")
    private String code;

    @ApiModelProperty(value = "模版标题", required = true)
    @NotNull(message = "模版标题不能为空")
    private String title;

    @ApiModelProperty(value = "模版内容", required = true)
    @NotNull(message = "模版内容不能为空")
    private String content;

    @ApiModelProperty(value = "状态：1-启用 0-禁用", required = true)
    @NotNull(message = "状态：1-启用 0-禁用不能为空")
    @InEnum(value = CommonStatusEnum.class, message = "状态必须是 {value}")
    private Integer status;

    @ApiModelProperty(value = "备注")
    private String remarks;

}
