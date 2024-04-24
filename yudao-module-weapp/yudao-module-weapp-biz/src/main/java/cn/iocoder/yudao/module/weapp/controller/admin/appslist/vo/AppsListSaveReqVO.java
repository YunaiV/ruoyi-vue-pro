package cn.iocoder.yudao.module.weapp.controller.admin.appslist.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 小程序清单新增/修改 Request VO")
@Data
public class AppsListSaveReqVO {

    @Schema(description = "主键ID;主键iD", requiredMode = Schema.RequiredMode.REQUIRED, example = "5789")
    private Integer id;

    @Schema(description = "小程序名称;小程序名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @NotEmpty(message = "小程序名称;小程序名称不能为空")
    private String weappName;

    @Schema(description = "小程序OPENID;小程序OPENID", requiredMode = Schema.RequiredMode.REQUIRED, example = "27787")
    @NotEmpty(message = "小程序OPENID;小程序OPENID不能为空")
    private String weappOpenid;

    @Schema(description = "分类ID;所属分类ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "26012")
    @NotEmpty(message = "分类ID;所属分类ID不能为空")
    private String classId;

    @Schema(description = "小程序简介;小程序说明", example = "你说的对")
    private String description;

    @Schema(description = "小程序图标;小程序图标")
    private String logoImg;

    @Schema(description = "状态;状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "状态;状态不能为空")
    private String status;

    @Schema(description = "更新人;更新人")
    private String updatedBy;

    @Schema(description = "更新时间;数据更新时间")
    private LocalDateTime updatedTime;

}