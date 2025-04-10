package cn.iocoder.yudao.module.srm.controller.admin.purchase.payment.term.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 付款条款 Response VO")
@Data
@ExcelIgnoreUnannotated
public class SrmPaymentTermRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "10931")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "人民币采购条款（中文）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("人民币采购条款（中文）")
    private String paymentTermCn;

    @Schema(description = "外币采购条款（中文）")
    @ExcelProperty("外币采购条款（中文）")
    private String paymentTermCnForeign;

    @Schema(description = "外币采购条款（英文）")
    @ExcelProperty("外币采购条款（英文）")
    private String paymentTermEnForeign;

    @Schema(description = "备注", example = "随便")
    @ExcelProperty("备注")
    private String remark;

}