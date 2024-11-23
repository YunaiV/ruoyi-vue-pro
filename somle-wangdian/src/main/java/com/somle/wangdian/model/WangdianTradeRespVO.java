package com.somle.wangdian.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class WangdianTradeRespVO extends WangdianRespVO {
    /**
     * 订单列表 (一级数据节点)
     */
    private List<TradeVO> trades;

    /**
     * Trade Data VO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradeVO {

        private Integer tradeId; // erp订单表主键
        private String tradeNo; // 订单编号
        private Short platformId; // 平台ID
        private Short shopPlatformId; // 店铺平台ID
        private String shopNo; // 店铺编号
        private String shopName; // 店铺名称
        private String shopRemark; // 店铺备注
        private Byte warehouseType; // 仓库类型
        private String warehouseNo; // 仓库编号
        private String srcTids; // 原始单号
        private Byte tradeStatus; // 订单状态
        private Integer consignStatus; // 发货状态
        private Byte tradeType; // 订单类型
        private Byte deliveryTerm; // 发货条件
        private Short freezeReason; // 冻结原因
        private Byte refundStatus; // 退款状态
        private Byte fenxiaoType; // 分销类别
        private String fenxiaoNick; // 分销商信息
        private String customerName; // 客户名称
        private String customerNo; // 客户编码
        private String payAccount; // 买家付款账号
        private String buyerNick; // 客户网名
        private String receiverName; // 收件人
        private String receiverAddress; // 地址
        private String receiverMobile; // 手机
        private String receiverTelno; // 电话
        private String receiverZip; // 邮编
        private String logisticsNo; // 物流单号
        private String logisticsName; // 物流公司名称
        private String logisticsCode; // 物流公司编号
        private BigDecimal goodsAmount; // 货品总额
        private BigDecimal discount; // 折扣
        private BigDecimal receivable; // 应收金额
        private BigDecimal weight; // 预估重量
        private BigDecimal tax; // 税额

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime tradeTime; // 下单时间

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime payTime; // 付款时间

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime modified; // 最后修改时间

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime created; // 系统订单生成时间

        /**
         * 货品列表 (二级数据节点)
         */
        private List<GoodsVO> goodsList;
    }

    /**
     * Goods Data VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoodsVO {

        private Integer recId; // 子订单主键
        private Integer tradeId; // erp订单主键
        private Integer specId; // erp内商品主键
        private String srcOid; // 子订单编号
        private String goodsName; // 货品名称
        private String specName; // 规格名称
        private BigDecimal price; // 标价
        private BigDecimal actualNum; // 实发数量
        private BigDecimal sharePrice; // 分摊后价格
        private BigDecimal discount; // 总折扣金额
        private BigDecimal weight; // 估重
        private Byte goodsType; // 货品类别

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime modified; // 最后修改时间

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime created; // 创建时间
    }
}
