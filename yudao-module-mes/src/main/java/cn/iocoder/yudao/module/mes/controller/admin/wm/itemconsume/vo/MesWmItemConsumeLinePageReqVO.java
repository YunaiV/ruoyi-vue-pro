package cn.iocoder.yudao.module.mes.controller.admin.wm.itemconsume.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 物料消耗记录行分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesWmItemConsumeLinePageReqVO extends PageParam {

    @Schema(description = "报工记录编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long feedbackId;

}
