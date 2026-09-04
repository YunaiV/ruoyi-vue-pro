package cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - PMS 工作项看板 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PmsWorkItemBoardReqVO extends PmsWorkItemPageReqVO {

    @Override
    @NotNull(message = "工作项类型不能为空")
    public Integer getType() {
        return super.getType();
    }

}
