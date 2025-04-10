package cn.iocoder.yudao.module.srm.controller.admin.purchase.payment.term.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SrmPaymentTermSimpleRespVO {
    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "10931")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "人民币采购条款（中文）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("人民币采购条款（中文）")
    private String paymentTermZh;

    @Schema(description = "外币采购条款（中文）")
    @ExcelProperty("外币采购条款（中文）")
    private String paymentTermZhForeign;

    @Schema(description = "外币采购条款（英文）")
    @ExcelProperty("外币采购条款（英文）")
    private String paymentTermEnForeign;
}
