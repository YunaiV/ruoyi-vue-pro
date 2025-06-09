package cn.iocoder.yudao.module.tms.controller.admin.first.mile.request.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.framework.mybatis.core.vo.BaseVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.request.item.vo.TmsFirstMileRequestItemRespVO;
import cn.iocoder.yudao.module.tms.controller.admin.vessel.tracking.vo.TmsVesselTrackingRespVO;
import cn.iocoder.yudao.module.tms.enums.TmsDictTypeConstants;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - 头程申请单 Response VO")
@Data
@ExcelIgnoreUnannotated
@Accessors(chain = false)
public class TmsFirstMileRequestRespVO extends BaseVO {

    @Schema(description = "主单编号")
    private Long id;

    @Schema(description = "单据编码")
    @ExcelProperty("编码")
    private String code;

    @Schema(description = "申请人ID")
    private Long requesterId;

    @Schema(description = "申请人名称")
    @ExcelProperty("申请人名称")
    private String requestUserName;

    @Schema(description = "申请部门ID")
    private Long requestDeptId;

    @Schema(description = "申请部门名称")
    @ExcelProperty("申请部门名称")
    private String requestDeptName;

    @Schema(description = "目的仓ID")
    private Long toWarehouseId;

    @Schema(description = "目的仓名称")
    @ExcelProperty("目的仓名称")
    private String toWarehouseName;

    @Schema(description = "审核状态")
    @ExcelProperty(value = "审核状态", converter = DictConvert.class)
    @DictFormat(TmsDictTypeConstants.AUDIT_STATUS)
    private Integer auditStatus;

    @Schema(description = "订购状态")
    @ExcelProperty(value = "订购状态", converter = DictConvert.class)
    @DictFormat(TmsDictTypeConstants.ORDER_STATUS)
    private Integer orderStatus;

    @Schema(description = "关闭状态")
    @ExcelProperty(value = "关闭状态", converter = DictConvert.class)
    @DictFormat(TmsDictTypeConstants.OFF_STATUS)
    private Integer offStatus;

    @Schema(description = "总重量（kg）")
    @ExcelProperty("总重量（kg）")
    private BigDecimal totalWeight;

    @Schema(description = "总体积（m³）")
    @ExcelProperty("总体积（m³）")
    private BigDecimal totalVolume;

    @Schema(description = "明细数量")
    @ExcelProperty("明细数量")
    private Integer itemCount;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "版本号")
    private Integer revision;

    @Schema(description = "头程申请表明细列表")
    private List<TmsFirstMileRequestItemRespVO> items;

    @Schema(description = "船期信息")
    private TmsVesselTrackingRespVO vesselTracking;
}