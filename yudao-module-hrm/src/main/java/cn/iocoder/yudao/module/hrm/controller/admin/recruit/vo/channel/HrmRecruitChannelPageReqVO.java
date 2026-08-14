package cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.channel;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - HRM 招聘渠道分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class HrmRecruitChannelPageReqVO extends PageParam {

    @Schema(description = "渠道名称，模糊匹配", example = "BOSS")
    private String name;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
