package cn.iocoder.yudao.module.mp.controller.admin.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Schema(description = "管理后台 - 公众号粉丝 Response VO")
@Data
public class MpUserRespVO  {

    @Schema(description = "编号", required = true, example = "1024")
    private Long id;

    @Schema(description = "公众号粉丝标识", required = true, example = "o6_bmjrPTlm6_2sgVt7hMZOPfL2M")
    private String openid;

    @Schema(description = "关注状态 参见 CommonStatusEnum 枚举", required = true, example = "1")
    private Integer subscribeStatus;
    @Schema(description = "关注时间", required = true)
    private LocalDateTime subscribeTime;
    @Schema(description = "取消关注时间")
    private LocalDateTime unsubscribeTime;

    @Schema(description = "昵称", example = "芋道")
    private String nickname;
    @Schema(description = "头像地址", example = "https://www.iocoder.cn/1.png")
    private String headImageUrl;
    @Schema(description = "语言", example = "zh_CN")
    private String language;
    @Schema(description = "国家", example = "中国")
    private String country;
    @Schema(description = "省份", example = "广东省")
    private String province;
    @Schema(description = "城市", example = "广州市")
    private String city;
    @Schema(description = "备注", example = "你是一个芋头嘛")
    private String remark;

    @Schema(description = "标签编号数组", example = "1,2,3")
    private List<Long> tagIds;

    @Schema(description = "公众号账号的编号", required = true, example = "1")
    private Long accountId;
    @Schema(description = "公众号账号的 appId", required = true, example = "wx1234567890")
    private String appId;

    @Schema(description = "创建时间", required = true)
    private Date createTime;

}
