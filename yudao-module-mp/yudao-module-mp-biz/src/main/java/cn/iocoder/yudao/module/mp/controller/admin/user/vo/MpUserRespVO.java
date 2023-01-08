package cn.iocoder.yudao.module.mp.controller.admin.user.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@ApiModel("管理后台 - 公众号粉丝 Response VO")
@Data
public class MpUserRespVO  {

    @ApiModelProperty(value = "编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "公众号用户标识", required = true, example = "o6_bmjrPTlm6_2sgVt7hMZOPfL2M")
    private String openid;

    @ApiModelProperty(value = "关注状态", required = true, example = "1", notes = "参见 CommonStatusEnum 枚举")
    private Integer subscribeStatus;
    @ApiModelProperty(value = "关注时间", required = true)
    private LocalDateTime subscribeTime;
    @ApiModelProperty(value = "取消关注时间")
    private LocalDateTime unsubscribeTime;

    @ApiModelProperty(value = "昵称", example = "芋道")
    private String nickname;
    @ApiModelProperty(value = "头像地址", example = "https://www.iocoder.cn/1.png")
    private String headImageUrl;
    @ApiModelProperty(value = "语言", example = "zh_CN")
    private String language;
    @ApiModelProperty(value = "国家", example = "中国")
    private String country;
    @ApiModelProperty(value = "省份", example = "广东省")
    private String province;
    @ApiModelProperty(value = "城市", example = "广州市")
    private String city;
    @ApiModelProperty(value = "备注", example = "你是一个芋头嘛")
    private String remark;

    @ApiModelProperty(value = "标签编号数组", example = "1,2,3")
    private List<Long> tagIds;

    @ApiModelProperty(value = "公众号账号的编号", required = true, example = "1")
    private Long accountId;
    @ApiModelProperty(value = "公众号账号的 appId", required = true, example = "wx1234567890")
    private String appId;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
