package cn.iocoder.yudao.module.im.controller.admin.manager.channel.vo.channel;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - IM 频道分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ImChannelPageReqVO extends PageParam {

    @Schema(description = "频道业务码", example = "system_notice")
    private String code;

    @Schema(description = "频道名称", example = "系统")
    private String name;

    @Schema(description = "状态", example = "0")
    private Integer status; // 参见 CommonStatusEnum 枚举类

}
