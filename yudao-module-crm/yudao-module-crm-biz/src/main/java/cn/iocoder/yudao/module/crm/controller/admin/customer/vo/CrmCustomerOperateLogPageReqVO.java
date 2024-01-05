package cn.iocoder.yudao.module.crm.controller.admin.customer.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - crm 客户操作日志分页 Request VO")
@Data
public class CrmCustomerOperateLogPageReqVO extends PageParam {

    @Schema(description = "模块数据编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long bizId;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "模块类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private String bizType;

}
