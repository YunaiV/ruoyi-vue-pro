package cn.iocoder.yudao.module.crm.controller.admin.business.vo.status;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.Collection;

@Schema(description = "管理后台 - 商机状态 Query VO")
@Data
@ToString(callSuper = true)
public class CrmBusinessStatusQueryVO {

    @Schema(description = "主键集合")
    private Collection<Long> idList;

    @Schema(description = "状态类型编号")
    private Long typeId;
}
