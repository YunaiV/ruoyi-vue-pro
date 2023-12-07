package cn.iocoder.yudao.module.promotion.controller.admin.discount.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 限时折扣活动 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DiscountActivityRespVO extends DiscountActivityBaseVO {

    @Schema(description = "活动编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "活动状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "活动状态不能为空")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;


    @Schema(description = "商品 SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048") // TODO @zhangshuai：属性和属性之间，最多空一行噢；
    private Long spuId;

    @Schema(description = "限时折扣商品", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<DiscountActivityBaseVO.Product> products;

    // ========== 商品字段 ==========

    // TODO @zhangshuai：一个优惠活动，会关联多个商品，所以它不用返回 spuName 哈；
    // TODO 最终界面展示字段就：编号、活动名称、参与商品数、活动状态、开始时间、结束时间、操作
    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED, // 从 SPU 的 name 读取
            example = "618大促")
    private String spuName;
    @Schema(description = "商品主图", requiredMode = Schema.RequiredMode.REQUIRED, // 从 SPU 的 picUrl 读取
            example = "https://www.iocoder.cn/xx.png")
    private String picUrl;
    @Schema(description = "商品市场价，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, // 从 SPU 的 marketPrice 读取
            example = "50")
    private Integer marketPrice;

}
