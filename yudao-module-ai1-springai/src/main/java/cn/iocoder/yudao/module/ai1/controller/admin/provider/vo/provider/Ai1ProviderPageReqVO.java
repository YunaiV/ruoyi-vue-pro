package cn.iocoder.yudao.module.ai1.controller.admin.provider.vo.provider;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - AI1 Provider 分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Ai1ProviderPageReqVO extends PageParam {

    @Schema(description = "名称，模糊匹配", example = "DeepSeek")
    private String name;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
