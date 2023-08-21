package cn.iocoder.yudao.module.member.controller.admin.level.vo.level;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author owen
 */
@Schema(description = "管理后台 - 会员等级分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MemberLevelPageReqVO extends PageParam {

    @Schema(description = "等级名称", example = "芋艿")
    private String name;

    @Schema(description = "状态", example = "1")
    private Integer status;

}
