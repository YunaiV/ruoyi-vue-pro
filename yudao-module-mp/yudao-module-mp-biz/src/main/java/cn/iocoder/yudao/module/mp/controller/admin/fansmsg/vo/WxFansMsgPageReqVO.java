package cn.iocoder.yudao.module.mp.controller.admin.fansmsg.vo;

import lombok.*;

import java.util.*;

import io.swagger.annotations.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel("管理后台 - 粉丝消息表 分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WxFansMsgPageReqVO extends PageParam {

    @ApiModelProperty(value = "用户标识")
    private String openid;

    @ApiModelProperty(value = "昵称")
    private String nickname;

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

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "开始创建时间")
    private Date beginCreateTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "结束创建时间")
    private Date endCreateTime;

}
