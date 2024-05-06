package cn.iocoder.yudao.module.weapp.controller.admin.appsclass.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 小程序分类 Response VO")
@Data
@ExcelIgnoreUnannotated
public class AppsClassRespVO {

    @Schema(description = "主键ID;主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "23303")
    @ExcelProperty("主键ID;主键ID")
    private Integer id;

    @Schema(description = "分类名;分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("分类名;分类名称")
    private String className;

    @Schema(description = "状态;是否启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态;是否启用")
    private String status;

    @Schema(description = "创建时间;创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间;创建时间")
    private LocalDateTime createTime;

}