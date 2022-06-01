package cn.iocoder.yudao.module.mp.admin.accountfans.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
* 微信公众号粉丝 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class WxAccountFansBaseVO {

    @ApiModelProperty(value = "用户标识")
    private String openid;

    @ApiModelProperty(value = "订阅状态，0未关注，1已关注")
    private String subscribeStatus;

    @ApiModelProperty(value = "订阅时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date subscribeTime;

    @ApiModelProperty(value = "昵称")
    private byte[] nickname;

    @ApiModelProperty(value = "性别，1男，2女，0未知")
    private String gender;

    @ApiModelProperty(value = "语言")
    private String language;

    @ApiModelProperty(value = "国家")
    private String country;

    @ApiModelProperty(value = "省份")
    private String province;

    @ApiModelProperty(value = "城市")
    private String city;

    @ApiModelProperty(value = "头像地址")
    private String headimgUrl;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "微信公众号ID")
    private String wxAccountId;

    @ApiModelProperty(value = "微信公众号appid")
    private String wxAccountAppid;

}
