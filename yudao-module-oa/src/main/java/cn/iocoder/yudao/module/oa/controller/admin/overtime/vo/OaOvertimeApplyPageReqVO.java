package cn.iocoder.yudao.module.oa.controller.admin.overtime.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 加班申请分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OaOvertimeApplyPageReqVO extends PageParam {

    @Schema(description = "标题", example = "项目上线加班申请")
    private String title;

    @Schema(description = "审批状态", example = "-1")
    private Integer status;

}
