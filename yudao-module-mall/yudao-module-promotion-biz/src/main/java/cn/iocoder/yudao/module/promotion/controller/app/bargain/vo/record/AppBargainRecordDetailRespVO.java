package cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.record;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 App - 砍价记录的明细 Response VO")
@Data
public class AppBargainRecordDetailRespVO {

    public static final int HELP_ACTION_NONE = 1; // 帮砍动作 - 未帮砍，可以帮砍
    public static final int HELP_ACTION_FULL = 2; // 帮砍动作 - 未帮砍，无法帮砍（可帮砍次数已满)
    public static final int HELP_ACTION_SUCCESS = 3; // 帮砍动作 - 已帮砍

    private Long id;
    private Long userId;
    private Long spuId;
    private Long skuId;
    private Long activityId;
    private Integer bargainPrice;
    private Integer price;
    private Integer payPrice;
    private Integer status;

    private LocalDateTime expireTime;

    private Long orderId;
    private Boolean payStatus;

    private Integer helpAction;

}
