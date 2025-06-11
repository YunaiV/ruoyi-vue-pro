package cn.iocoder.yudao.module.tms.controller.admin.first.mile.request.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.request.item.vo.TmsFirstMileRequestItemPageReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 头程申请单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TmsFirstMileRequestPageReqVO extends PageParam {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "创建时间")
    private LocalDateTime[] createTime;

    @Schema(description = "单据编号")
    private String code;

    @Schema(description = "申请人ID")
    private Long requesterId;

    @Schema(description = "申请部门ID")
    private Long requestDeptId;

    @Schema(description = "目的仓ID")
    private Long toWarehouseId;

    @Schema(description = "审核状态")
    private Integer auditStatus;

    @Schema(description = "订购状态")
    private Integer orderStatus;

    @Schema(description = "关闭状态")
    private Integer offStatus;

    private TmsFirstMileRequestItemPageReqVO item;
}