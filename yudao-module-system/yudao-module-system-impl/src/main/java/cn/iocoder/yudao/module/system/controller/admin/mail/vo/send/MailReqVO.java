package cn.iocoder.yudao.module.system.controller.admin.mail.vo.send;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@ApiModel("管理后台 - 邮件发送 Req VO")
@Data
public class MailReqVO { // TODO @wangjingqi：1）, 不用空格；2）应该只要传递 templateCode、参数就好，title、from、content、附件应该都是参数里的

    @ApiModelProperty(value = "邮箱" , required = true , example = "yudaoyuanma@123.com")
    @NotNull(message = "邮箱账号不能为空")
    private String from;

    @ApiModelProperty(value = "标题"  , example = "标题")
    private String title;

    @ApiModelProperty(value = "内容"  , example = "内容")
    private String content;

    @ApiModelProperty(value = "邮箱模版id" , example = "1024")
    @NotNull(message = "邮箱模版id不能为空")
    private Integer templateId;

    @ApiModelProperty(value = "收件人" , required = true , example = "yudaoyuanma@123.com")
    @NotNull(message = "收件人不能为空")
    private List<String> tos;

    @ApiModelProperty(value = "附件"  , example = "附件编码")
    private List<String> fileIds;


}
