package cn.iocoder.yudao.module.im.controller.admin.manager.face.vo.pack;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IM 表情包 Response VO")
@Data
public class ImFacePackRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "表情包名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "猫主子")
    private String name;

    @Schema(description = "表情包图标", example = "https://cdn.example.com/face/pack/cat.png")
    private String icon;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer sort;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status; // 参见 CommonStatusEnum 枚举类

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
