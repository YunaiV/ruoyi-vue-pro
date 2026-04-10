package cn.iocoder.yudao.module.crm.controller.admin.business.vo.status;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 商机状态 Response VO")
@Data
public class CrmBusinessStatusRespVO {

    @Schema(description = "状态组编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2934")
    private Long id;

    @Schema(description = "状态组名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    private String name;

    @Schema(description = "使用的部门编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> deptIds;
    @Schema(description = "使用的部门名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> deptNames;

    @Schema(description = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String creator;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "状态集合", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Status> statuses;

    @Data
    public static class Status {

        @Schema(description = "状态编号", example = "23899")
        private Long id;

        @Schema(description = "状态名", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
        private String name;

        @Schema(description = "赢单率", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
        private BigDecimal percent;

        @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer sort;

    }

}
