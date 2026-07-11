package cn.iocoder.yudao.module.crm.controller.admin.performance.vo.config;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.crm.enums.performance.CrmPerformanceConfigBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.performance.CrmPerformanceConfigObjectTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - CRM 业绩目标设置分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmPerformanceConfigPageReqVO extends PageParam {

    @Schema(description = "目标对象编号", example = "1")
    private Long objectId;

    @Schema(description = "目标对象类型", example = "2")
    @InEnum(value = CrmPerformanceConfigObjectTypeEnum.class, message = "目标对象类型，必须是 {value}")
    private Integer objectType;

    @Schema(description = "年份", example = "2026")
    private Integer year;

    @Schema(description = "目标类型", example = "5")
    @InEnum(value = CrmPerformanceConfigBizTypeEnum.class, message = "目标类型，必须是 {value}")
    private Integer bizType;

}
