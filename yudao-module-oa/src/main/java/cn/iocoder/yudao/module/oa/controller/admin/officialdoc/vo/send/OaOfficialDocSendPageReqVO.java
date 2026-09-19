package cn.iocoder.yudao.module.oa.controller.admin.officialdoc.vo.send;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 公文发文分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OaOfficialDocSendPageReqVO extends PageParam {

    @Schema(description = "公文标题", example = "关于开展办公用品盘点的通知")
    private String title;

    @Schema(description = "发文字号", example = "芋办〔2026〕1号")
    private String documentNo;

    @Schema(description = "流程状态", example = "-1")
    private Integer status;

}
