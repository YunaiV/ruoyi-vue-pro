package cn.iocoder.yudao.module.crm.controller.admin.business.vo.type;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.Collection;

@Schema(description = "管理后台 - 商机状态类型 Query VO")
@Data
@ToString(callSuper = true)
public class CrmBusinessStatusTypeQueryVO {

    @Schema(description = "主键集合")
    private Collection<Long> idList;

    @Schema(description = "状态")
    private Integer status;
}
