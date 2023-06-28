package cn.iocoder.yudao.module.member.controller.admin.point.vo.recrod;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理后台 - 用户积分记录 Excel 导出 Request VO，参数和 PointRecordPageReqVO 是一致的")
@Data
public class MemberPointRecordExportReqVO {

    @Schema(description = "业务编码", example = "22706")
    private String bizId;

    @Schema(description = "业务类型", example = "1")
    private String bizType;

    @Schema(description = "1增加 0扣减", example = "1")
    private String type;

    @Schema(description = "积分标题")
    private String title;

    @Schema(description = "状态：1-订单创建，2-冻结期，3-完成，4-失效（订单退款） ", example = "1")
    private Integer status;

}
