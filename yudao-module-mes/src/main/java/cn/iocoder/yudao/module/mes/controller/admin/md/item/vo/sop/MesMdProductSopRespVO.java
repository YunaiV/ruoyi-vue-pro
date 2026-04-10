package cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.sop;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 产品SOP Response VO")
@Data
public class MesMdProductSopRespVO {

    @Schema(description = "SOP编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "物料产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "69")
    private Long itemId;

    @Schema(description = "排列顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer sort;

    @Schema(description = "工序ID", example = "100")
    private Long processId;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "步骤一")
    private String title;

    @Schema(description = "详细描述", example = "操作说明")
    private String description;

    @Schema(description = "图片地址", example = "https://www.example.com/sop.png")
    private String url;

    @Schema(description = "备注", example = "备注信息")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "工序编号", example = "PROC001")
    private String processCode;

    @Schema(description = "工序名称", example = "焊接工序")
    private String processName;

}
