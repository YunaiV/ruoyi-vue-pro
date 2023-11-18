package cn.iocoder.yudao.module.crm.controller.admin.businessstatus.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 商机状态分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmBusinessStatusPageReqVO extends PageParam {

    @Schema(description = "状态类型编号", example = "22882")
    private Long typeId;

}
