package cn.iocoder.yudao.module.system.controller.admin.mail.vo.template;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class MailTemplatePageReqVO extends PageParam {
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("标识")
    private String code;

    @ApiModelProperty("发件人")
    private String username;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("状态")
    private String status;

    @ApiModelProperty("备注")
    private String remark;
}
