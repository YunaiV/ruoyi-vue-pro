package cn.iocoder.yudao.module.fms.controller.admin.finance.subject.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - Erp财务主体 精简列表")
@Data
@ExcelIgnoreUnannotated
public class FmsFinanceSubjectSimpleRespVO {
    @Schema(description = "id")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "主体名称")
    @ExcelProperty("主体名称")
    private String name;

    @Schema(description = "纳税人识别号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("纳税人识别号")
    private String taxNo;

    @Schema(description = "开户行")
    @ExcelProperty("开户行")
    private String bankName;

    @Schema(description = "开户账号")
    @ExcelProperty("开户账号")
    private String bankAccount;

    @Schema(description = "开户地址")
    @ExcelProperty("开户地址")
    private String bankAddress;

    @Schema(description = "联系人")
    @ExcelProperty("联系人")
    private String contact;

    @Schema(description = "手机号码")
    @ExcelProperty("手机号码")
    private String mobile;

    @Schema(description = "联系电话")
    @ExcelProperty("联系电话")
    private String telephone;

    @Schema(description = "电子邮箱")
    @ExcelProperty("电子邮箱")
    private String email;

    @Schema(description = "传真")
    @ExcelProperty("传真")
    private String fax;

    @Schema(description = "送达地址")
    @ExcelProperty("送达地址")
    private String deliveryAddress;

    @Schema(description = "公司地址")
    @ExcelProperty("公司地址")
    private String companyAddress;
}
