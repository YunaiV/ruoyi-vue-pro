package cn.iocoder.yudao.module.fms.controller.admin.voucher.vo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * FMS 凭证导入 Excel VO
 *
 * @author 芋道源码
 */
@Data
public class FmsVoucherImportExcelVO {

    /**
     * Excel 行号
     */
    private Integer rowNumber;

    /**
     * 凭证日期
     */
    @NotNull(message = "日期不能为空或格式不正确")
    private LocalDateTime voucherTime;

    /**
     * 凭证字
     */
    @NotBlank(message = "凭证字不能为空")
    private String voucherWordName;

    /**
     * 凭证号
     */
    @NotNull(message = "凭证号不能为空")
    @Min(value = 1, message = "凭证号必须为正整数")
    private Integer voucherNumber;

    /**
     * 附件数
     */
    @Min(value = 0, message = "附件数不能小于 0")
    private Integer attachmentCount;

    /**
     * 摘要内容
     */
    private String digest;

    /**
     * 科目编码
     */
    @NotBlank(message = "科目编码不能为空")
    private String subjectCode;

    /**
     * 科目名称
     */
    private String subjectName;

    /**
     * 借方金额
     */
    @Digits(integer = 16, fraction = 2, message = "借方金额最多保留 2 位小数")
    private BigDecimal debitAmount;

    /**
     * 贷方金额
     */
    @Digits(integer = 16, fraction = 2, message = "贷方金额最多保留 2 位小数")
    private BigDecimal creditAmount;

    /**
     * 数量
     */
    @DecimalMin(value = "0", message = "数量不能小于 0")
    @Digits(integer = 14, fraction = 4, message = "数量最多保留 4 位小数")
    private BigDecimal quantity;

    /**
     * 单价
     */
    @DecimalMin(value = "0", message = "单价不能小于 0")
    @Digits(integer = 12, fraction = 6, message = "单价最多保留 6 位小数")
    private BigDecimal unitPrice;

    /**
     * 辅助核算类别编号与项目编码的映射
     */
    private Map<Long, String> auxiliaryCodes = new LinkedHashMap<>();
    /**
     * Excel 表头与原始单元格值的映射
     */
    private Map<String, String> sourceValues = new LinkedHashMap<>();
    /**
     * 校验错误数组
     */
    private List<String> errors = new ArrayList<>();

    public void addError(String error) {
        if (!errors.contains(error)) {
            errors.add(error);
        }
    }

}
