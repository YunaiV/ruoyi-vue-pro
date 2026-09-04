package cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.template;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - PMS 项目模板分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class PmsProjectTemplatePageReqVO extends PageParam {

    @Schema(description = "模板名称，模糊匹配", example = "敏捷研发模板")
    private String name;

    @Schema(description = "项目类型", example = "2")
    @InEnum(PmsProjectTypeEnum.class)
    private Integer projectType;

    @Schema(description = "模板状态", example = "0")
    private Integer status;

}
