package cn.iocoder.yudao.module.mes.controller.admin.pro.process.vo.content;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 生产工序内容 Response VO")
@Data
public class MesProProcessContentRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "工序编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long processId;

    @Schema(description = "顺序编号", example = "1")
    private Integer sort;

    @Schema(description = "步骤说明", example = "检查焊接设备是否正常")
    private String content;

    @Schema(description = "辅助设备", example = "氩弧焊机")
    private String device;

    @Schema(description = "辅助材料", example = "焊丝 ER308")
    private String material;

    @Schema(description = "材料文档 URL", example = "https://example.com/doc.pdf")
    private String docUrl;

    @Schema(description = "备注", example = "注意安全")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
