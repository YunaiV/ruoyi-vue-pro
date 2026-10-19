package cn.iocoder.yudao.module.oa.controller.admin.regular.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 转正申请分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OaRegularApplyPageReqVO extends PageParam {

    @Schema(description = "标题", example = "试用期转正申请")
    private String title;

    @Schema(description = "审批状态", example = "-1")
    private Integer status;

}
