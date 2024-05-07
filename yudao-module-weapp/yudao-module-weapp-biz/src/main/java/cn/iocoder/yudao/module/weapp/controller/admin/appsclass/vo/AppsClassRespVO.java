package cn.iocoder.yudao.module.weapp.controller.admin.appsclass.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;

@Schema(description = "管理后台 - 小程序分类 Response VO")
@Data
@ExcelIgnoreUnannotated
public class AppsClassRespVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "24526")
    @ExcelProperty("主键ID")
    private Integer id;

    @Schema(description = "分类名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("分类名")
    private String className;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat("weapp_publish_status") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private String status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "排序", example = "1")
    @ExcelProperty("排序")
    private Integer indexNum;

}