package cn.iocoder.yudao.module.mp.controller.admin.message.vo;

import lombok.*;
import io.swagger.annotations.*;

/**
 * 粉丝消息表  Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class MpMessageBaseVO {

    @ApiModelProperty(value = "用户标识")
    private String openid;

    @ApiModelProperty(value = "昵称")
    private byte[] nickname;

    @ApiModelProperty(value = "头像地址")
    private String headimgUrl;

    @ApiModelProperty(value = "微信账号ID")
    private String wxAccountId;

    @ApiModelProperty(value = "消息类型")
    private String msgType;

    @ApiModelProperty(value = "内容")
    private String content;

    @ApiModelProperty(value = "最近一条回复内容")
    private String resContent;

    @ApiModelProperty(value = "是否已回复")
    private String isRes;

    @ApiModelProperty(value = "微信素材ID")
    private String mediaId;

    @ApiModelProperty(value = "微信图片URL")
    private String picUrl;

    @ApiModelProperty(value = "本地图片路径")
    private String picPath;

}
