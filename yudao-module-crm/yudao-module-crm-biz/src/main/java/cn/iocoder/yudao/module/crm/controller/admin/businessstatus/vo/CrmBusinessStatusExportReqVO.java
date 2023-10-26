package cn.iocoder.yudao.module.crm.controller.admin.businessstatus.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

@Schema(description = "管理后台 - 商机状态 Excel 导出 Request VO，参数和 CrmBusinessStatusPageReqVO 是一致的")
@Data
public class CrmBusinessStatusExportReqVO {

    @Schema(description = "状态类型编号", example = "22882")
    private Long typeId;

    @Schema(description = "状态名", example = "李四")
    private String name;

    @Schema(description = "赢单率")
    private String percent;

    @Schema(description = "排序")
    private Integer sort;

}
