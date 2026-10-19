package cn.iocoder.yudao.module.oa.controller.admin.leave.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 请假申请分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OaLeaveApplyPageReqVO extends PageParam {

    @Schema(description = "标题", example = "个人事务请假申请")
    private String title;

    @Schema(description = "审批状态", example = "-1")
    private Integer status;

}
