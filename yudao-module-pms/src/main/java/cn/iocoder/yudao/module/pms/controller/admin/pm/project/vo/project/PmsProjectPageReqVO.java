package cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.project;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectSceneTypeEnum;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectSortTypeEnum;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - PMS 项目分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PmsProjectPageReqVO extends PageParam {

    @Schema(description = "项目名称", example = "官网")
    private String name;

    @Schema(description = "列表场景", example = "1")
    @InEnum(PmsProjectSceneTypeEnum.class)
    private Integer sceneType = PmsProjectSceneTypeEnum.ALL.getType();

    @Schema(description = "个人项目分组编号", example = "1024")
    private Long groupId;

    @Schema(description = "项目状态", example = "1")
    @InEnum(PmsProjectStatusEnum.class)
    private Integer status = PmsProjectStatusEnum.ACTIVE.getStatus();

    @Schema(description = "排序类型", example = "1")
    @InEnum(PmsProjectSortTypeEnum.class)
    private Integer sortType = PmsProjectSortTypeEnum.ACCESS_TIME.getType();

}
