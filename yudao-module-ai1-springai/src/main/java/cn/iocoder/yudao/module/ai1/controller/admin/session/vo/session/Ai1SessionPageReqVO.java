package cn.iocoder.yudao.module.ai1.controller.admin.session.vo.session;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - AI1 会话分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Ai1SessionPageReqVO extends PageParam {

    @Schema(description = "Agent 编号，为空时查询全部 Agent", example = "1")
    private Long agentId;

    @Schema(description = "标题，模糊匹配", example = "三体")
    private String title;

    @Schema(description = "用户编号", example = "1")
    private Long userId;

}
