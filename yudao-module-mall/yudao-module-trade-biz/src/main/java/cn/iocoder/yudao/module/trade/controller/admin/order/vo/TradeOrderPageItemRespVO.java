package cn.iocoder.yudao.module.trade.controller.admin.order.vo;

import cn.iocoder.yudao.module.trade.controller.admin.base.member.user.MemberUserRespVO;
import cn.iocoder.yudao.module.trade.controller.admin.base.product.property.ProductPropertyValueDetailRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 交易订单的分页项 Response VO")
@Data
public class TradeOrderPageItemRespVO extends TradeOrderBaseVO {

    @Schema(description = "收件人地区名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "上海 上海市 普陀区")
    private String receiverAreaName;

    @Schema(description = "订单项列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Item> items;

    @Schema(description = "用户信息", requiredMode = Schema.RequiredMode.REQUIRED)
    private MemberUserRespVO user;

    @Schema(description = "推广人信息")
    private MemberUserRespVO brokerageUser;

    @Schema(description = "管理后台 - 交易订单的分页项的订单项目")
    @Data
    public static class Item extends TradeOrderItemBaseVO {

        @Schema(description = "属性列表", requiredMode = Schema.RequiredMode.REQUIRED)
        private List<ProductPropertyValueDetailRespVO> properties;

    }

}
