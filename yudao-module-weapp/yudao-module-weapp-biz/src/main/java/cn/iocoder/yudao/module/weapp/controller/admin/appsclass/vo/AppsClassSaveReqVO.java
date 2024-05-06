package cn.iocoder.yudao.module.weapp.controller.admin.appsclass.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 小程序分类新增/修改 Request VO")
@Data
public class AppsClassSaveReqVO {

    @Schema(description = "主键ID;主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "23303")
    private Integer id;

    @Schema(description = "分类名;分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotEmpty(message = "分类名;分类名称不能为空")
    private String className;

    @Schema(description = "状态;是否启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "状态;是否启用不能为空")
    private String status;

}