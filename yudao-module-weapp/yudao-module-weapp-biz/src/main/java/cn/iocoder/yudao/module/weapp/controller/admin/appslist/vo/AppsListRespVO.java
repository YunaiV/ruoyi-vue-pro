package cn.iocoder.yudao.module.weapp.controller.admin.appslist.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.util.*;
import com.alibaba.excel.annotation.*;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;

@Schema(description = "管理后台 - 小程序清单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class AppsListRespVO {

    @Schema(description = "小程序名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @ExcelProperty("小程序名称")
    private String weappName;

    @Schema(description = "小程序OPENID", requiredMode = Schema.RequiredMode.REQUIRED, example = "25136")
    @ExcelProperty("小程序OPENID")
    private String weappOpenid;

    @Schema(description = "分类ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "5963")
    @ExcelProperty("分类ID")
    private String classId;

    @Schema(description = "小程序图标")
    @ExcelProperty("小程序图标")
    private String logoImg;

    @Schema(description = "发布状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "已发布")
    @ExcelProperty(value = "发布状态", converter = DictConvert.class)
    @DictFormat("weapp_publish_status") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer status;

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "27728")
    @ExcelProperty("主键ID")
    private Long id;

}