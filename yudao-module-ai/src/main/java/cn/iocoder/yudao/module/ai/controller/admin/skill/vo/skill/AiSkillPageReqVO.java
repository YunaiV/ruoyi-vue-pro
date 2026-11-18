package cn.iocoder.yudao.module.ai.controller.admin.skill.vo.skill;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - AI 技能分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AiSkillPageReqVO extends PageParam {

    @Schema(description = "技能名称，模糊匹配", example = "order-analysis")
    private String name;

    @Schema(description = "状态", example = "0")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

}
