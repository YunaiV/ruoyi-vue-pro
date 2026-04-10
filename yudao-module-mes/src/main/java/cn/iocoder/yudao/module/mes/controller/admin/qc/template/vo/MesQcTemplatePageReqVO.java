package cn.iocoder.yudao.module.mes.controller.admin.qc.template.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 质检方案分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesQcTemplatePageReqVO extends PageParam {

    @Schema(description = "方案编号", example = "QCT001")
    private String code;

    @Schema(description = "方案名称", example = "出货检验")
    private String name;

    @Schema(description = "检测种类", example = "1")
    private Integer type;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
