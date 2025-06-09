package cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.supplier;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.payment.term.vo.SrmPaymentTermRespVO;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 供应商 Response VO")
@Data
@ExcelIgnoreUnannotated
public class SrmSupplierRespVO {

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17791")
    @ExcelProperty(value = "供应商编号", index = 0)
    private Long id;

    @Schema(description = "供应商名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道源码")
    @ExcelProperty(value = "供应商名称", index = 1)
    private String name;

    @Schema(description = "联系人", example = "芋艿")
    @ExcelProperty(value = "联系人", index = 2)
    private String contact;

    @Schema(description = "手机号码", example = "15601691300")
    @ExcelProperty(value = "手机号码", index = 3)
    private String mobile;

    @Schema(description = "联系电话", example = "18818288888")
    @ExcelProperty(value = "联系电话", index = 4)
    private String telephone;

    @Schema(description = "电子邮箱", example = "76853@qq.com")
    @ExcelProperty(value = "电子邮箱", index = 5)
    private String email;

    @Schema(description = "传真", example = "20 7123 4567")
    @ExcelProperty(value = "传真", index = 6)
    private String fax;

    @Schema(description = "备注", example = "你猜")
    @ExcelProperty(value = "备注", index = 7)
    private String remark;

    @Schema(description = "开启状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "开启状态", index = 8, converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer openStatus;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @ExcelProperty(value = "排序", index = 9)
    private Integer sort;

    @Schema(description = "纳税人识别号", example = "91130803MA098BY05W")
    @ExcelProperty(value = "纳税人识别号", index = 10)
    private String taxNo;

    @Schema(description = "税率", example = "10")
    @ExcelProperty(value = "税率", index = 11)
    @NumberFormat(pattern = "#.##%")
    private BigDecimal taxRate;

    @Schema(description = "开户行", example = "张三")
    @ExcelProperty(value = "开户行", index = 12)
    private String bankName;

    @Schema(description = "开户账号", example = "622908212277228617")
    @ExcelProperty(value = "开户账号", index = 13)
    private String bankAccount;

    @Schema(description = "开户地址", example = "兴业银行浦东支行")
    @ExcelProperty(value = "开户地址", index = 14)
    private String bankAddress;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "创建时间", index = 15)
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime createTime;

    @Schema(description = "付款条款ID")
    @ExcelProperty(value = "付款条款ID", index = 16)
    private Long paymentTermsId;

    @Schema(description = "付款条款中文")
    private String paymentTerms;

    @Schema(description = "付款条款")
    private SrmPaymentTermRespVO srmPaymentTermsResp;

    @Schema(description = "送达地址")
    @ExcelProperty(value = "送达地址", index = 17)
    private String deliveryAddress;

    @Schema(description = "公司地址")
    @ExcelProperty(value = "公司地址", index = 18)
    private String companyAddress;
}