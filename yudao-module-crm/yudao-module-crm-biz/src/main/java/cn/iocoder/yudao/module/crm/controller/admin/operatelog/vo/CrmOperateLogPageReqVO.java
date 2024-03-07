package cn.iocoder.yudao.module.crm.controller.admin.operatelog.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - CRM 操作日志 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmOperateLogPageReqVO extends PageParam {

    @Schema(description = "数据类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @InEnum(CrmBizTypeEnum.class)
    @NotNull(message = "数据类型不能为空")
    private Integer bizType;

    @Schema(description = "数据编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "数据编号不能为空")
    private Long bizId;

}
