package cn.iocoder.yudao.module.crm.controller.admin.contact.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - CRM 联系人 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ExcelIgnoreUnannotated
public class CrmContactRespVO extends CrmContactBaseVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "3167")
    private Long id;

    @Schema(description = "创建时间")
    @ExcelProperty(value = "创建时间", order = 8)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @ExcelProperty(value = "更新时间", order = 8)
    private LocalDateTime updateTime;

    @Schema(description = "创建人", example = "25682")
    private String creator;

    @Schema(description = "创建人名字", example = "test")
    @ExcelProperty(value = "创建人", order = 8)
    private String creatorName;

    @ExcelProperty(value = "客户名称",order = 2)
    @Schema(description = "客户名字", example = "test")
    private String customerName;

    @Schema(description = "负责人", example = "test")
    @ExcelProperty(value = "负责人", order = 7)
    private String ownerUserName;

    @Schema(description = "直属上级名", example = "芋头")
    @ExcelProperty(value = "直属上级", order = 4)
    private String parentName;

    @Schema(description = "地区名", example = "上海上海市浦东新区")
    @ExcelProperty(value = "地区", order = 5)
    private String areaName;

}
