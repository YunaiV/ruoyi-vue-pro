package cn.iocoder.yudao.module.ai1.controller.admin.agent.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - AI1 Agent 分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Ai1AgentPageReqVO extends PageParam {

    @Schema(description = "名称，模糊匹配", example = "客服助手")
    private String name;

    // TODO @AI：类似这种，inenum 就好了，不用写“参见 CommonStatusEnum 枚举”把；
    @Schema(description = "状态，参见 CommonStatusEnum 枚举", example = "0")
    private Integer status;

}
