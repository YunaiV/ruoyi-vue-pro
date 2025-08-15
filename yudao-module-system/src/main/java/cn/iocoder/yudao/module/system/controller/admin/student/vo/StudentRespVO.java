package cn.iocoder.yudao.module.system.controller.admin.student.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 学生主子表测试 Response VO")
@Data
@ExcelIgnoreUnannotated
public class StudentRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "29522")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @ExcelProperty("名字")
    private String name;

    @Schema(description = "出生日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("出生日期")
    private LocalDateTime birthday;

    @Schema(description = "简介", requiredMode = Schema.RequiredMode.REQUIRED, example = "随便")
    @ExcelProperty("简介")
    private String description;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}