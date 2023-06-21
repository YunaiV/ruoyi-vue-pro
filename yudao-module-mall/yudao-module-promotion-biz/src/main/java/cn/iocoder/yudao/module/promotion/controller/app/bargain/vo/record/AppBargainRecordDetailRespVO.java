package cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.record;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 App - 砍价记录的明细 Response VO")
@Data
public class AppBargainRecordDetailRespVO {

    public static final int ACTION_NONE = 1; // 参与动作 - 未参与，可参与
    public static final int ACTION_WAITING = 2; // 参与动作 - 参与中，等待砍价
    public static final int ACTION_SUCCESS = 3; // 参与动作 - 砍价成功，待下单
    public static final int ACTION_ORDER_CREATE = 4; // 参与动作 - 已下单，未付款
    public static final int ACTION_ORDER_PAY = 5; // 参与动作 - 已下单，已付款

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

    private Long orderId; // 有且仅在自己的订单才返回

    private Integer action;
    private Integer helpAction;

    // 【未参与】1-可以参与砍价：storeBargainUser 为空；
    // 【未参与】2-参与次数已满：storeBargainUser 为空，但是砍价次数已满；storeBargainUser 非空已支付，但是砍价次数已满 TODO 芋艿：这个就不做了
    // 【参与中】3-砍价中：storeBargainUser 非空，人数未满
    // 【参与中】4-已完成：storeBargainUser 非空，未创建订单

    // 支付的情况；
    // 8-已生成订单未支付：storeBargainUser 不为空（status = 3），但是订单未支付；
    // 9-已支付：storeBargainUser 不为空（status = 3），且订单已支付；
    // 10-取消支付：storeBargainUser 不为空（status = 3），取消支付；【可忽略】

    // help 的情况；
    // 5-可以帮砍：和 3 对应；
    // 6-已帮砍：其他人的，有帮砍过；
    // 7-帮砍次数已满：其他人的，帮砍次数已满


}
