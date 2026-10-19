package cn.iocoder.yudao.module.oa.controller.admin.officialdoc.vo.receive;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 公文收文分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OaOfficialDocReceivePageReqVO extends PageParam {

    @Schema(description = "公文标题", example = "关于开展办公用品盘点的通知")
    private String title;

    @Schema(description = "来文字号", example = "芋办〔2026〕1号")
    private String documentNo;

    @Schema(description = "流程状态", example = "-1")
    private Integer status;

}
