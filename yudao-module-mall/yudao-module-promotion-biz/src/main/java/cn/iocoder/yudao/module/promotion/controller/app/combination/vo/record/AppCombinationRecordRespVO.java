package cn.iocoder.yudao.module.promotion.controller.app.combination.vo.record;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Schema(description = "用户 App - 拼团记录 Response VO")
@Data
public class AppCombinationRecordRespVO {

    @Schema(description = "拼团记录编号", required = true, example = "1024")
    private Long id;

    @Schema(description = "拼团活动编号", required = true, example = "1024")
    private Long activityId;

    @Schema(description = "用户昵称", required = true, example = "1024")
    private String nickname;

    @Schema(description = "用户头像", required = true, example = "1024")
    private String avatar;

    @Schema(description = "过期时间", required = true)
    private Date expireTime;

    @Schema(description = "可参团人数", required = true, example = "10")
    private Integer userSize;

    @Schema(description = "已参团人数", required = true, example = "5")
    private Integer userCount;

    @Schema(description = "拼团状态", required = true, example = "1")
    private Integer status;

    @Schema(description = "商品图片", required = true, example = "https://www.iocoder.cn/1.png")
    private String picUrl;

    @Schema(description = "拼团金额，单位：分", required = true, example = "100")
    private Integer combinationPrice;

    // ========== 获得最近 n 条拼团记录（团长发起的）返回的字段 ==========

    @Schema(description = "拼团活动名字", required = true, example = "1024")
    private String activityName;

}
