package cn.iocoder.yudao.module.mes.controller.admin.dv.maintenrecord.vo.line;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 设备保养记录明细分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesDvMaintenRecordLinePageReqVO extends PageParam {

    @Schema(description = "保养记录ID", example = "1")
    private Long recordId;

}
