package cn.iocoder.yudao.module.ai1.controller.admin.provider.vo.model;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - AI1 模型分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Ai1ModelPageReqVO extends PageParam {

    @Schema(description = "供应商编号", example = "1")
    private Long providerId;

    @Schema(description = "展示名称，模糊匹配", example = "DeepSeek")
    private String name;

    @Schema(description = "类型", example = "0")
    private Integer type;

}
