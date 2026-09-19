package cn.iocoder.yudao.module.oa.controller.admin.officialdoc.vo.template;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 套红模板分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OaOfficialDocTemplatePageReqVO extends PageParam {

    @Schema(description = "模板名称", example = "公司行政发文模板")
    private String name;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
