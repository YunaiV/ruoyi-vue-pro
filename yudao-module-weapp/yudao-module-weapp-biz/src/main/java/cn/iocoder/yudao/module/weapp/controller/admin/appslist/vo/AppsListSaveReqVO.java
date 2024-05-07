package cn.iocoder.yudao.module.weapp.controller.admin.appslist.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 小程序清单新增/修改 Request VO")
@Data
public class AppsListSaveReqVO {

    @Schema(description = "小程序名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotEmpty(message = "小程序名称不能为空")
    private String weappName;

    @Schema(description = "小程序OPENID", requiredMode = Schema.RequiredMode.REQUIRED, example = "25136")
    @NotEmpty(message = "小程序OPENID不能为空")
    private String weappOpenid;


    @Schema(description = "banner展示", requiredMode = Schema.RequiredMode.REQUIRED, example = "是")
    @NotEmpty(message = "小程序OPENID不能为空")
    private String isBanner;

    @Schema(description = "分类ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "5963")
    @NotEmpty(message = "分类ID不能为空")
    private String classId;

    @Schema(description = "小程序图标")
    private String logoImg;

    @Schema(description = "发布状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "已发布")
    @NotNull(message = "发布状态不能为空")
    private Integer status;

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "27728")
    private Long id;

}