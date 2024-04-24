package cn.iocoder.yudao.module.weapp.controller.admin.appslist.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 小程序清单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class AppsListRespVO {

    @Schema(description = "主键ID;主键iD", requiredMode = Schema.RequiredMode.REQUIRED, example = "5789")
    @ExcelProperty("主键ID;主键iD")
    private Integer id;

    @Schema(description = "小程序名称;小程序名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @ExcelProperty("小程序名称;小程序名称")
    private String weappName;

    @Schema(description = "小程序OPENID;小程序OPENID", requiredMode = Schema.RequiredMode.REQUIRED, example = "27787")
    @ExcelProperty("小程序OPENID;小程序OPENID")
    private String weappOpenid;

    @Schema(description = "分类ID;所属分类ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "26012")
    @ExcelProperty("分类ID;所属分类ID")
    private String classId;

    @Schema(description = "小程序简介;小程序说明", example = "你说的对")
    @ExcelProperty("小程序简介;小程序说明")
    private String description;

    @Schema(description = "小程序图标;小程序图标")
    @ExcelProperty("小程序图标;小程序图标")
    private String logoImg;

    @Schema(description = "状态;状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态;状态")
    private String status;

    @Schema(description = "更新人;更新人")
    @ExcelProperty("更新人;更新人")
    private String updatedBy;

    @Schema(description = "更新时间;数据更新时间")
    @ExcelProperty("更新时间;数据更新时间")
    private LocalDateTime updatedTime;

}