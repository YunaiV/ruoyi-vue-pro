package cn.iocoder.yudao.module.highway.controller.admin.project.vo;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

@Schema(description = "管理后台 - 项目管理分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProjectPageReqVO extends PageParam {

    @Schema(description = "项目编号")
    private String code;

    @Schema(description = "项目名称", example = "王五")
    private String pname;

    @Schema(description = "项目描述")
    private String description;

}