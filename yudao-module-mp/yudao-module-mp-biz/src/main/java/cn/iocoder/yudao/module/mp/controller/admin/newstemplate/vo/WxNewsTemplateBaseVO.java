package cn.iocoder.yudao.module.mp.controller.admin.newstemplate.vo;

import lombok.*;
import io.swagger.annotations.*;

/**
 * 图文消息模板 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class WxNewsTemplateBaseVO {

    @ApiModelProperty(value = "模板名称")
    private String tplName;

    @ApiModelProperty(value = "是否已上传微信")
    private String isUpload;

    @ApiModelProperty(value = "素材ID")
    private String mediaId;

    @ApiModelProperty(value = "微信账号ID")
    private String wxAccountId;

}
