package cn.iocoder.yudao.module.crm.controller.admin.business.vo.type;

import cn.iocoder.yudao.module.crm.controller.admin.business.vo.status.CrmBusinessStatusSaveReqVO;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "管理后台 - 商机状态类型新增/修改 Request VO")
@Data
public class CrmBusinessStatusTypeSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "2934")
    private Long id;

    @Schema(description = "状态类型名", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotEmpty(message = "状态类型名不能为空")
    private String name;

    @Schema(description = "使用的部门编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> deptIds = Lists.newArrayList();

    // TODO @ljlleo VO 里面，我们不使用默认值哈。这里 Lists.newArrayList() 看看怎么去掉。上面 deptIds 也是类似噢
    @Schema(description = "商机状态集合", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CrmBusinessStatusSaveReqVO> statusList = Lists.newArrayList();

}
