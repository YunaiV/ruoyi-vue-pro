package cn.iocoder.yudao.module.mes.controller.admin.pro.andon.vo.config;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 安灯呼叫配置分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesProAndonConfigPageReqVO extends PageParam {

    @Schema(description = "呼叫原因", example = "设备故障")
    private String reason;

    @Schema(description = "级别", example = "1")
    private Integer level;

}
