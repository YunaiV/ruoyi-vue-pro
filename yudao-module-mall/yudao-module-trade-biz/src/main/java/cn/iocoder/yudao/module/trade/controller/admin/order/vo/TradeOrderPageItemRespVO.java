package cn.iocoder.yudao.module.trade.controller.admin.order.vo;

import cn.iocoder.yudao.module.trade.controller.admin.base.product.property.ProductPropertyValueDetailRespVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel("管理后台 - 交易订单的分页项 Response VO")
@Data
public class TradeOrderPageItemRespVO extends TradeOrderBaseVO {

    @ApiModelProperty(value = "收件人地区名字", required = true, example = "上海 上海市 普陀区")
    private String receiverAreaName;

    /**
     * 订单项列表
     */
    private List<Item> items;

    @ApiModel("管理后台 - 交易订单的分页项的订单项目")
    @Data
    public static class Item extends TradeOrderItemBaseVO {

        /**
         * 属性数组
         */
        private List<ProductPropertyValueDetailRespVO> properties;

    }

}
