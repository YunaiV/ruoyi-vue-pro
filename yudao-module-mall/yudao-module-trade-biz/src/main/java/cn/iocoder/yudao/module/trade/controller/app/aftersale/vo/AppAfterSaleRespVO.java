package cn.iocoder.yudao.module.trade.controller.app.aftersale.vo;

import cn.iocoder.yudao.module.trade.controller.app.base.property.AppProductPropertyValueDetailRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "用户 App - 交易售后 Response VO")
@Data
public class AppAfterSaleRespVO {

    @Schema(description = "售后编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "售后流水号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1146347329394184195")
    private String no;

    @Schema(description = "售后状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "售后方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer way;

    @Schema(description = "售后类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "申请原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private String applyReason;

    @Schema(description = "补充描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private String applyDescription;

    @Schema(description = "补充凭证图片", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private List<String> applyPicUrls;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

    // ========== 交易订单相关 ==========

    @Schema(description = "交易订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long orderId;

    @Schema(description = "交易订单流水号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private String orderNo;

    @Schema(description = "交易订单项编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long orderItemId;

    @Schema(description = "商品 SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long spuId;

    @Schema(description = "商品 SPU 名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private String spuName;

    @Schema(description = "商品 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long skuId;

    /**
     * 属性数组
     */
    private List<AppProductPropertyValueDetailRespVO> properties;

    @Schema(description = "商品图片", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/01.jpg")
    private String picUrl;

    @Schema(description = "退货商品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer count;

    // ========== 审批相关 ==========

    /**
     * 审批备注
     *
     * 注意，只有审批不通过才会填写
     */
    private String auditReason;

    // ========== 退款相关 ==========

    @Schema(description = "退款金额，单位：分", example = "100")
    private Integer refundPrice;

    @Schema(description = "退款时间")
    private LocalDateTime refundTime;

    // ========== 退货相关 ==========

    @Schema(description = "退货物流公司编号", example = "1")
    private Long logisticsId;

    @Schema(description = "退货物流单号", example = "SF123456789")
    private String logisticsNo;

    @Schema(description = "退货时间")
    private LocalDateTime deliveryTime;

    @Schema(description = "收货时间")
    private LocalDateTime receiveTime;

    @Schema(description = "收货备注")
    private String receiveReason;

}
