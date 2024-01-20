package cn.iocoder.yudao.module.crm.controller.admin.followup.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 跟进记录新增/修改 Request VO")
@Data
public class CrmFollowUpRecordSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "28800")
    private Long id;

    @Schema(description = "数据类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "数据类型不能为空")
    private Integer bizType;

    @Schema(description = "数据编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "5564")
    @NotNull(message = "数据编号不能为空")
    private Long bizId;

    @Schema(description = "跟进类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "跟进类型不能为空")
    private Integer type;

    @Schema(description = "跟进内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "跟进内容不能为空")
    private String content;

    @Schema(description = "下次联系时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "下次联系时间不能为空")
    private LocalDateTime nextTime;

    @Schema(description = "关联的商机编号数组")
    private List<Long> businessIds;
    @Schema(description = "关联的联系人编号数组")
    private List<Long> contactIds;

    @Schema(description = "图片")
    private List<String> picUrls;
    @Schema(description = "附件")
    private List<String> fileUrls;

}