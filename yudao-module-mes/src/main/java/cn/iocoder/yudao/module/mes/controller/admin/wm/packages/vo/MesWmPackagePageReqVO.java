package cn.iocoder.yudao.module.mes.controller.admin.wm.packages.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * MES 装箱单分页 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - MES 装箱单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesWmPackagePageReqVO extends PageParam {

    @Schema(description = "装箱单编号", example = "PKG")
    private String code;

    @Schema(description = "销售订单编号", example = "SO")
    private String salesOrderCode;

    @Schema(description = "客户 ID", example = "1")
    private Long clientId;

    @Schema(description = "父箱 ID", example = "1024")
    private Long parentId;

    @Schema(description = "检查员用户 ID", example = "1")
    private Long inspectorUserId;

    @Schema(description = "单据状态", example = "1")
    private Integer status;

}
