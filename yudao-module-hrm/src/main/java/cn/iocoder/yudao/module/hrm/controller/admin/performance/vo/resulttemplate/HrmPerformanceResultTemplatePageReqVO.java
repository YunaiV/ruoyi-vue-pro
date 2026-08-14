package cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.resulttemplate;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - HRM 绩效结果模板分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class HrmPerformanceResultTemplatePageReqVO extends PageParam {

    @Schema(description = "结果模板名称，模糊匹配", example = "季度")
    private String name;

}
