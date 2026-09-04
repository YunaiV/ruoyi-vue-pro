package cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.favorite;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.pms.enums.kb.PmsKnowledgeObjectTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - PMS 知识收藏（关注）保存 Request VO")
@Data
public class PmsKnowledgeFavoriteSaveReqVO {

    @Schema(description = "对象类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @NotNull(message = "对象类型不能为空")
    @InEnum(value = PmsKnowledgeObjectTypeEnum.class, message = "对象类型必须是 {value}")
    private Integer type;

    @Schema(description = "对象编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "对象编号不能为空")
    private Long entityId;

}
