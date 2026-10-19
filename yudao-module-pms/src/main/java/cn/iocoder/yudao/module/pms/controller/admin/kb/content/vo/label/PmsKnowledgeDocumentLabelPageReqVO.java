package cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.label;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - PMS 按标签查询文档分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PmsKnowledgeDocumentLabelPageReqVO extends PageParam {

    @Schema(description = "文档标签编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "文档标签编号不能为空")
    private Long labelId;

}
