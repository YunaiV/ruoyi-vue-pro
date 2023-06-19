package cn.iocoder.yudao.module.point.controller.admin.pointrecord.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

@Schema(description = "管理后台 - 用户积分记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PointRecordPageReqVO extends PageParam {

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
