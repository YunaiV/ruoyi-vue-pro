package cn.iocoder.yudao.module.mes.controller.admin.md.client.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 客户 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesMdClientRespVO {

    @Schema(description = "客户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("客户编号")
    private Long id;

    @Schema(description = "客户编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "C00184")
    @ExcelProperty("客户编码")
    private String code;

    @Schema(description = "客户名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "比亚迪")
    @ExcelProperty("客户名称")
    private String name;

    @Schema(description = "客户简称", example = "比亚迪")
    @ExcelProperty("客户简称")
    private String nickname;

    @Schema(description = "客户英文名称", example = "BYD")
    @ExcelProperty("客户英文名称")
    private String englishName;

    @Schema(description = "客户简介", example = "比亚迪品牌诞生于深圳")
    @ExcelProperty("客户简介")
    private String description;

    @Schema(description = "客户LOGO地址", example = "https://xxx.com/logo.png")
    private String logo;

    @Schema(description = "客户类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "客户类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.MES_CLIENT_TYPE)
    private Integer type;

    @Schema(description = "客户地址", example = "深圳南山区")
    @ExcelProperty("客户地址")
    private String address;

    @Schema(description = "客户官网地址", example = "https://www.bydglobal.com")
    @ExcelProperty("客户官网地址")
    private String website;

    @Schema(description = "客户邮箱地址", example = "salse@bydglobal.com")
    @ExcelProperty("客户邮箱地址")
    private String email;

    @Schema(description = "客户电话", example = "123432222")
    @ExcelProperty("客户电话")
    private String telephone;

    @Schema(description = "联系人1", example = "张三")
    @ExcelProperty("联系人1")
    private String contact1Name;

    @Schema(description = "联系人1-电话", example = "122212312")
    @ExcelProperty("联系人1-电话")
    private String contact1Telephone;

    @Schema(description = "联系人1-邮箱", example = "s1@bydglobal.com")
    @ExcelProperty("联系人1-邮箱")
    private String contact1Email;

    @Schema(description = "联系人2", example = "李四")
    @ExcelProperty("联系人2")
    private String contact2Name;

    @Schema(description = "联系人2-电话", example = "1132323232")
    @ExcelProperty("联系人2-电话")
    private String contact2Telephone;

    @Schema(description = "联系人2-邮箱", example = "s2@bydglobal.com")
    @ExcelProperty("联系人2-邮箱")
    private String contact2Email;

    @Schema(description = "统一社会信用代码", example = "11212121")
    @ExcelProperty("统一社会信用代码")
    private String creditCode;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(cn.iocoder.yudao.module.system.enums.DictTypeConstants.COMMON_STATUS)
    private Integer status;

    @Schema(description = "备注", example = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
