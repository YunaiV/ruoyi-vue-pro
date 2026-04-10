package cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.sip;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - MES 产品SIP新增/修改 Request VO")
@Data
public class MesMdProductSipSaveReqVO {

    @Schema(description = "SIP编号", example = "1024")
    private Long id;

    @Schema(description = "物料产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "69")
    @NotNull(message = "物料产品ID不能为空")
    private Long itemId;

    @Schema(description = "排列顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "排列顺序不能为空")
    private Integer sort;

    @Schema(description = "工序ID", example = "100")
    private Long processId;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "步骤一")
    @NotEmpty(message = "标题不能为空")
    private String title;

    @Schema(description = "详细描述", example = "操作说明")
    private String description;

    @Schema(description = "图片地址", example = "https://www.example.com/sip.png")
    private String url;

    @Schema(description = "备注", example = "备注信息")
    private String remark;

}
