package cn.iocoder.yudao.module.system.controller.admin.mail.vo.send;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
@Data
public class MailSendVO {

    @ApiModelProperty(value = "邮箱" , required = true , example = "yudaoyuanma@123.com")
    private String from;

    @ApiModelProperty(value = "标题"  , example = "标题")
    private String title;

    @ApiModelProperty(value = "内容"  , example = "内容")
    private String content;

    @ApiModelProperty(value = "收件人" , required = true , example = "yudaoyuanma@123.com")
    private List<String> tos;

    @ApiModelProperty(value = "附件"  , example = "附件编码")
    private List<String> fileIds;


}
