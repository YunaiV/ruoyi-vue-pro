package cn.iocoder.yudao.module.oa.controller.admin.note.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - OA 笔记共享修改 Request VO")
@Data
public class OaNoteShareUpdateReqVO {

    @Schema(description = "笔记编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "笔记编号不能为空")
    private Long id;

    @Schema(description = "接收人用户编号列表，空列表表示取消全部共享", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1, 2]")
    @NotNull(message = "接收人列表不能为空")
    private List<@NotNull(message = "接收人编号不能为空") Long> receiverUserIds;

}
