package cn.iocoder.yudao.module.mes.controller.admin.dv.subject.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 点检保养项目分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesDvSubjectPageReqVO extends PageParam {

    @Schema(description = "项目编码", example = "CHK001")
    private String code;

    @Schema(description = "项目名称", example = "注塑机外观检查")
    private String name;

    @Schema(description = "项目类型", example = "1")
    private Integer type;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
