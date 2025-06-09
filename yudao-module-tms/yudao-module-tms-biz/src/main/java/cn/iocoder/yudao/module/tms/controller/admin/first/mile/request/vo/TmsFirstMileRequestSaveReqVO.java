package cn.iocoder.yudao.module.tms.controller.admin.first.mile.request.vo;

import cn.iocoder.yudao.module.system.api.utils.Validation;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.request.item.vo.TmsFirstMileRequestItemSaveReqVO;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

import static cn.iocoder.yudao.module.tms.dal.redis.no.TmsNoRedisDAO.FIRST_MILE_REQUEST_NO_PREFIX;

@Schema(description = "管理后台 - 头程申请单新增/修改 Request VO")
@Data
public class TmsFirstMileRequestSaveReqVO {

    @Schema(description = "id")
    @Null(groups = Validation.OnCreate.class, message = "创建时，id必须为空")
    @NotNull(groups = Validation.OnUpdate.class, message = "更新时，id不能为空")
    @DiffLogField(name = "申请单ID")
    private Long id;

    @Schema(description = "单据编号")
    @Pattern(regexp = "^" + FIRST_MILE_REQUEST_NO_PREFIX + "-\\d{8}-\\d{6}$", message = "单据编号格式不正确")
    @DiffLogField(name = "单据编号")
    private String code;

    @Schema(description = "申请人ID")
    @NotNull(message = "申请人不能为空")
    @DiffLogField(name = "申请人ID")
    private Long requesterId;

    @Schema(description = "申请部门ID")
    @NotNull(message = "申请部门不能为空")
    @DiffLogField(name = "申请部门ID")
    private Long requestDeptId;

    @Schema(description = "目的仓ID")
    @NotNull(message = "目的仓不能为空")
    @DiffLogField(name = "目的仓ID")
    private Long toWarehouseId;

    @Schema(description = "审核状态")
    @DiffLogField(name = "审核状态")
    private Integer auditStatus;

    @Schema(description = "备注")
    @DiffLogField(name = "备注")
    private String remark;

    @Schema(description = "版本号")
    @NotNull(groups = {Validation.OnUpdate.class}, message = "更新时版本号不能为空")
    private Integer revision;

    @Schema(description = "头程申请表明细列表")
    @Valid
    @Size(min = 1, message = "头程申请表明细列表不能为空")
    @DiffLogField(name = "头程申请表明细列表")
    private List<TmsFirstMileRequestItemSaveReqVO> items;

}