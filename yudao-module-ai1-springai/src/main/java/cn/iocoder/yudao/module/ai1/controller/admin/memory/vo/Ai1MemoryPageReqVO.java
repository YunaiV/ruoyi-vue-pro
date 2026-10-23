package cn.iocoder.yudao.module.ai1.controller.admin.memory.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - AI1 长期记忆分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Ai1MemoryPageReqVO extends PageParam {

    @Schema(description = "Agent 编号", example = "1")
    private Long agentId;

    @Schema(description = "用户编号", example = "1")
    private Long userId;

}
