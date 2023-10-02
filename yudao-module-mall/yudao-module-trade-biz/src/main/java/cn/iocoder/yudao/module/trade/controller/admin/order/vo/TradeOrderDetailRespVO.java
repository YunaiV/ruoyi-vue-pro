package cn.iocoder.yudao.module.trade.controller.admin.order.vo;

import cn.iocoder.yudao.module.trade.controller.admin.base.member.user.MemberUserRespVO;
import cn.iocoder.yudao.module.trade.controller.admin.base.product.property.ProductPropertyValueDetailRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 交易订单的详情 Response VO")
@Data
public class TradeOrderDetailRespVO extends TradeOrderBaseVO {

    /**
     * 订单项列表
     */
    private List<Item> items;

    /**
     * 下单用户信息
     */
    private MemberUserRespVO user;
    /**
     * 推广用户信息
     */
    private MemberUserRespVO brokerageUser;

    /**
     * 操作日志列表
     */
    private List<OrderLog> logs;

    @Schema(description = "收件人地区名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "上海 上海市 普陀区")
    private String receiverAreaName;

    @Schema(description = "管理后台 - 交易订单的操作日志")
    @Data
    public static class OrderLog {

        @Schema(description = "操作详情", requiredMode = Schema.RequiredMode.REQUIRED, example = "订单发货")
        private String content;

        @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2023-06-01 10:50:20")
        private LocalDateTime createTime;

        @Schema(description = "用户类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer userType;

    }

    @Schema(description = "管理后台 - 交易订单的详情的订单项目")
    @Data
    public static class Item extends TradeOrderItemBaseVO {

        /**
         * 属性数组
         */
        private List<ProductPropertyValueDetailRespVO> properties;

    }

}
