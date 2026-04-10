package cn.iocoder.yudao.module.mes.controller.admin.dv.repair.vo.line;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 维修工单行分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesDvRepairLinePageReqVO extends PageParam {

    @Schema(description = "维修工单编号", example = "1")
    private Long repairId;

}
