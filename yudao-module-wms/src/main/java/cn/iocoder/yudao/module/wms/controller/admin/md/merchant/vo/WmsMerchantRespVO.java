package cn.iocoder.yudao.module.wms.controller.admin.md.merchant.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.wms.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - WMS 往来企业 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsMerchantRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "往来企业编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "MER001")
    @ExcelProperty("往来企业编号")
    private String code;

    @Schema(description = "往来企业名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "华为")
    @ExcelProperty("往来企业名称")
    private String name;

    @Schema(description = "往来企业类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty(value = "往来企业类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.MERCHANT_TYPE)
    private Integer type;

    @Schema(description = "级别", example = "A")
    @ExcelProperty("级别")
    private String level;

    @Schema(description = "开户行", example = "招商银行")
    private String bankName;

    @Schema(description = "银行账户", example = "6225888888888888")
    private String bankAccount;

    @Schema(description = "地址", example = "深圳市南山区")
    private String address;

    @Schema(description = "手机号", example = "15601691300")
    private String mobile;

    @Schema(description = "座机号", example = "0755-88888888")
    private String telephone;

    @Schema(description = "联系人", example = "王五")
    @ExcelProperty("联系人")
    private String contact;

    @Schema(description = "Email", example = "wms@example.com")
    private String email;

    @Schema(description = "备注", example = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
