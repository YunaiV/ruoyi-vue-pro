package cn.iocoder.yudao.module.promotion.controller.app.point.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 积分商城活动 Response VO")
@Data
public class AppPointActivityRespVO {

    @Schema(description = "积分商城活动编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11373")
    @ExcelProperty("积分商城活动编号")
    private Long id;

    @Schema(description = "积分商城活动商品", requiredMode = Schema.RequiredMode.REQUIRED, example = "19509")
    @ExcelProperty("积分商城活动商品")
    private Long spuId;

    @Schema(description = "活动状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("活动状态")
    private Integer status;

    @Schema(description = "积分商城活动库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("积分商城活动库存")
    private Integer stock; // 剩余库存积分兑换时扣减

    @Schema(description = "积分商城活动总库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("积分商城活动总库存")
    private Integer totalStock;

    // ========== 商品字段 ==========

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED, // 从 SPU 的 name 读取
            example = "618大促")
    private String spuName;
    @Schema(description = "商品主图", requiredMode = Schema.RequiredMode.REQUIRED, // 从 SPU 的 picUrl 读取
            example = "https://www.iocoder.cn/xx.png")
    private String picUrl;
    @Schema(description = "商品市场价，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, // 从 SPU 的 marketPrice 读取
            example = "50")
    private Integer marketPrice;

    //======================= 显示所需兑换积分最少的 sku 信息 =======================

    @Schema(description = "兑换积分", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer point;

    @Schema(description = "兑换金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "15860")
    private Integer price;

}
