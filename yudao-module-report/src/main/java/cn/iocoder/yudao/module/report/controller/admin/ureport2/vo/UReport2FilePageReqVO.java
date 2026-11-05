package cn.iocoder.yudao.module.report.controller.admin.ureport2.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 管理后台 - UReport2 报表模板分页 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - UReport2 报表模板分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UReport2FilePageReqVO extends PageParam {

    @Schema(description = "模板名称，模糊匹配", example = "demo")
    private String name;

}
