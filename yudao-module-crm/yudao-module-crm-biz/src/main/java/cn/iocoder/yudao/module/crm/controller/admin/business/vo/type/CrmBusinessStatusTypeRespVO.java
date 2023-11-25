package cn.iocoder.yudao.module.crm.controller.admin.business.vo.type;

import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessStatusDO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 商机状态类型 Response VO")
@Data
@ExcelIgnoreUnannotated
public class CrmBusinessStatusTypeRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "2934")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "状态类型名", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("状态类型名")
    private String name;

    @Schema(description = "使用的部门编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("使用的部门编号")
    private List<Long> deptIds;
    @Schema(description = "使用的部门名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("使用的部门名称")
    private List<String> deptNames;

    @Schema(description = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建人")
    private String creator;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    // TODO @ljlleo 字段后缀改成 statuses，保持和 deptIds 风格一致；CrmBusinessStatusDO 改成 VO 哈；一般不使用 do 直接返回
    @Schema(description = "状态集合", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CrmBusinessStatusDO> statusList;

}
