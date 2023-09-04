package cn.iocoder.yudao.module.member.controller.app.brokerage.vo.record;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "应用 App - 分销记录分页 Request VO")
@Data
public class AppBrokerageRecordPageReqVO extends PageParam {

    // TODO @疯狂：要加下枚举校验

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer bizType;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

}
