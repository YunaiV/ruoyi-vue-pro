package cn.iocoder.yudao.module.crm.controller.admin.contactbusinesslink.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 联系人商机关联 Response VO")
@Data
@ExcelIgnoreUnannotated
public class CrmContactBusinessLinkRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "17220")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "联系人id", example = "20878")
    @ExcelProperty("联系人id")
    private Long contactId;

    @Schema(description = "商机id", example = "7638")
    @ExcelProperty("商机id")
    private Long businessId;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}