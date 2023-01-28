package cn.iocoder.yudao.module.system.controller.admin.notify.vo.template;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
* 站内信模版 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class NotifyTemplateBaseVO {

    @ApiModelProperty(value = "模版名称", required = true, example = "测试模版")
    @NotEmpty(message = "模版名称不能为空")
    private String name;

    @ApiModelProperty(value = "模版编码", required = true, example = "SEND_TEST")
    @NotNull(message = "模版编码不能为空")
    private String code;

    @ApiModelProperty(value = "模版类型", required = true, example = "1", notes = "对应 system_notify_template_type 字典")
    @NotNull(message = "模版类型不能为空")
    private Integer type;

    @ApiModelProperty(value = "发送人名称", required = true, example = "土豆")
    @NotEmpty(message = "发送人名称不能为空")
    private String nickname;

    @ApiModelProperty(value = "模版内容", required = true, example = "我是模版内容")
    @NotEmpty(message = "模版内容不能为空")
    private String content;

    @ApiModelProperty(value = "状态", required = true, example = "1", notes = "参见 CommonStatusEnum 枚举")
    @NotNull(message = "状态不能为空")
    @InEnum(value = CommonStatusEnum.class, message = "状态必须是 {value}")
    private Integer status;

    @ApiModelProperty(value = "备注", example = "我是备注")
    private String remark;

}
