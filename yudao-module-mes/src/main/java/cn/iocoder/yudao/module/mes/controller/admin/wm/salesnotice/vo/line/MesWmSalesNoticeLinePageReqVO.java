package cn.iocoder.yudao.module.mes.controller.admin.wm.salesnotice.vo.line;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 发货通知单行分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesWmSalesNoticeLinePageReqVO extends PageParam {

    @Schema(description = "发货通知单编号", example = "1")
    private Long noticeId;

}
