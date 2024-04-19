package cn.iocoder.yudao.module.crm.controller.admin.callcenter.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 用户与呼叫中心用户绑定关系 Response VO")
@Data
@ExcelIgnoreUnannotated
public class CrmCallcenterUserRespVO {

    @Schema(description = "主建", requiredMode = Schema.RequiredMode.REQUIRED, example = "30755")
    @ExcelProperty("主建")
    private Long id;

    @Schema(description = "用户名称", example = "2642")
    private String nickName;

    @Schema(description = "用户id", example = "2642")
    @ExcelProperty("用户id")
    private Long userId;

    @Schema(description = "云客用户id", example = "23969")
    @ExcelProperty("云客用户id")
    private String yunkeCallcenterUserId;

    @Schema(description = "云客手机号")
    @ExcelProperty("云客手机号")
    private String yunkeCallcenterPhone;

    @Schema(description = "连连用户id", example = "18461")
    @ExcelProperty("连连用户id")
    private String lianlianCallcenterUserId;

    @Schema(description = "连连手机号")
    @ExcelProperty("连连手机号")
    private String lianlianCallcenterPhone;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}