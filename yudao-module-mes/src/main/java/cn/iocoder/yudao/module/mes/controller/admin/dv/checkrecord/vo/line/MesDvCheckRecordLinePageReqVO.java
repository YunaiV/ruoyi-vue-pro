package cn.iocoder.yudao.module.mes.controller.admin.dv.checkrecord.vo.line;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 设备点检记录明细分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesDvCheckRecordLinePageReqVO extends PageParam {

    @Schema(description = "点检记录编号", example = "1")
    private Long recordId;

}
