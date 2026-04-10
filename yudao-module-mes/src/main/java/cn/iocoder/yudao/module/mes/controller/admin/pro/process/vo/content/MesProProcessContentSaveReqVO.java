package cn.iocoder.yudao.module.mes.controller.admin.pro.process.vo.content;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - MES 生产工序内容新增/修改 Request VO")
@Data
public class MesProProcessContentSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "工序编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "工序编号不能为空")
    private Long processId;

    @Schema(description = "顺序编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "顺序编号不能为空")
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

}
