package cn.iocoder.yudao.module.system.controller.admin.sms.vo.template;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
* 短信模板 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class SmsTemplateBaseVO {

    @ApiModelProperty(value = "短信类型", required = true, example = "1", notes = "参见 SmsTemplateTypeEnum 枚举类")
    @NotNull(message = "短信类型不能为空")
    private Integer type;

    @ApiModelProperty(value = "开启状态", required = true, example = "1", notes = "参见 CommonStatusEnum 枚举类")
    @NotNull(message = "开启状态不能为空")
    private Integer status;

    @ApiModelProperty(value = "模板编码", required = true, example = "test_01")
    @NotNull(message = "模板编码不能为空")
    private String code;

    @ApiModelProperty(value = "模板名称", required = true, example = "yudao")
    @NotNull(message = "模板名称不能为空")
    private String name;

    @ApiModelProperty(value = "模板内容", required = true, example = "你好，{name}。你长的太{like}啦！")
    @NotNull(message = "模板内容不能为空")
    private String content;

    @ApiModelProperty(value = "备注", example = "哈哈哈")
    private String remark;

    @ApiModelProperty(value = "短信 API 的模板编号", required = true, example = "4383920")
    @NotNull(message = "短信 API 的模板编号不能为空")
    private String apiTemplateId;

    @ApiModelProperty(value = "短信渠道编号", required = true, example = "10")
    @NotNull(message = "短信渠道编号不能为空")
    private Long channelId;

}
