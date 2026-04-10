package cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 工作站分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesMdWorkstationPageReqVO extends PageParam {

    @Schema(description = "工作站编码", example = "WK001")
    private String code;

    @Schema(description = "工作站名称", example = "一号工作站")
    private String name;

    @Schema(description = "所在车间编号", example = "1")
    private Long workshopId;

    @Schema(description = "工序编号", example = "1")
    private Long processId;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
