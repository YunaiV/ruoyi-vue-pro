package cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.record;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 App - 砍价记录的 Response VO")
@Data
public class AppBargainRecordRespVO {

    // TODO @芋艿：status；如果砍价对应的订单支付超时，算失败么？砍价的支付时间，以 expireTime 为准么？

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
    private Long payOrderId;

    private String activityName;
    private String picUrl;

}
