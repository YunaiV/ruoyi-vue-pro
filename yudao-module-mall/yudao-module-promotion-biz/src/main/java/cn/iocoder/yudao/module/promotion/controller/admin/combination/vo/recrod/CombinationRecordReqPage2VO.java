package cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.recrod;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 拼团记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CombinationRecordReqPage2VO extends PageParam {

    @Schema(description = "团长编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "团长编号不能为空")
    private Long headId;

}
