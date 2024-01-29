package cn.iocoder.yudao.module.promotion.controller.app.combination.vo.record;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 App - 拼团记录 Response VO")
@Data
public class AppCombinationRecordRespVO {

    @Schema(description = "拼团记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "拼团活动编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long activityId;

    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private String nickname;

    @Schema(description = "用户头像", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private String avatar;

    @Schema(description = "过期时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime expireTime;

    @Schema(description = "可参团人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer userSize;

    @Schema(description = "已参团人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer userCount;

    @Schema(description = "拼团状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long orderId;

    @Schema(description = "商品名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "我是大黄豆")
    private String spuName;

    @Schema(description = "商品图片", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/1.png")
    private String picUrl;

    @Schema(description = "购买的商品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer count;

    @Schema(description = "拼团金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer combinationPrice;

}
