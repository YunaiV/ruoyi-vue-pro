package cn.iocoder.yudao.module.trade.controller.admin.aftersale.vo;

import cn.iocoder.yudao.module.trade.controller.admin.aftersale.vo.log.TradeAfterSaleLogRespVO;
import cn.iocoder.yudao.module.trade.controller.admin.base.member.user.MemberUserRespVO;
import cn.iocoder.yudao.module.trade.controller.admin.base.product.property.ProductPropertyValueDetailRespVO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderBaseVO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderItemBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 售后订单的详情 Response VO")
@Data
public class TradeAfterSaleDetailRespVO extends TradeAfterSaleBaseVO {

    @Schema(description = "售后编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    /**
     * 订单项列表
     */
    private List<Item> items;

    /**
     * 订单基本信息
     */
    private TradeOrderBaseVO order;

    /**
     * 用户信息
     */
    private MemberUserRespVO user;

    /**
     * 售后日志
     */
    private List<TradeAfterSaleLogRespVO> afterSaleLog;

    @Schema(description = "管理后台 - 交易订单的详情的订单项目")
    @Data
    public static class Item extends TradeOrderItemBaseVO {

        /**
         * 属性数组
         */
        private List<ProductPropertyValueDetailRespVO> properties;

    }

}
