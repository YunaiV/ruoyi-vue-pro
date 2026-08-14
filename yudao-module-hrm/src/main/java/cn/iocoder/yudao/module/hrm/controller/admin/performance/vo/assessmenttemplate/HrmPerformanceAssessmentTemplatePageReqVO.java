package cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessmenttemplate;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - HRM 绩效考核模板分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class HrmPerformanceAssessmentTemplatePageReqVO extends PageParam {

    @Schema(description = "模板名称，模糊匹配", example = "季度")
    private String name;

}
