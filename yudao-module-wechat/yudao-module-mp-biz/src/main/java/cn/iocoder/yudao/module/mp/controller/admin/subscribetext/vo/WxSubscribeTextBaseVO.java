package cn.iocoder.yudao.module.mp.controller.admin.subscribetext.vo;

import lombok.*;
import io.swagger.annotations.*;

/**
 * 关注欢迎语 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class WxSubscribeTextBaseVO {

    @ApiModelProperty(value = "消息类型 1文本消息；2图文消息；")
    private String msgType;

    @ApiModelProperty(value = "模板ID")
    private String tplId;

    @ApiModelProperty(value = "微信账号ID")
    private String wxAccountId;

}
