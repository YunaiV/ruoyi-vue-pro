package cn.iocoder.yudao.module.mes.controller.admin.cal.team.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 班组分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesCalTeamPageReqVO extends PageParam {

    @Schema(description = "班组编码", example = "TEAM-A")
    private String code;

    @Schema(description = "班组名称", example = "注塑")
    private String name;

    @Schema(description = "班组类型", example = "1")
    private Integer calendarType;

}
