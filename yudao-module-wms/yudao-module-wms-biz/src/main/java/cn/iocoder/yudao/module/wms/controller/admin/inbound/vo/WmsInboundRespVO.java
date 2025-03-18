package cn.iocoder.yudao.module.wms.controller.admin.inbound.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import java.util.List;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemRespVO;

/**
 * @table-fields : tenant_id,no,creator,actual_arrival_time,create_time,creator_comment,source_bill_id,trace_no,type,refer_no,updater,update_time,plan_arrival_time,init_age,shipping_method,source_bill_no,source_bill_type,id,status,warehouse_id
 */
@Schema(description = "管理后台 - 入库单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsInboundRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "6889")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "单据号")
    @ExcelProperty("单据号")
    private String no;

    @Schema(description = "入库单类型 ; InboundType : 1-手工", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("入库单类型")
    private Integer type;

    @Schema(description = "仓库ID", example = "23620")
    @ExcelProperty("仓库ID")
    private Long warehouseId;

    @Schema(description = "入库单类型 ; InboundStatus : 0-起草中 , 0-待审批 , 0-已驳回 , 0-已通过", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("入库单类型")
    private Integer status;

    @Schema(description = "来源单据ID", example = "24655")
    @ExcelProperty("来源单据ID")
    private Long sourceBillId;

    @Schema(description = "来源单据号")
    @ExcelProperty("来源单据号")
    private String sourceBillNo;

    @Schema(description = "来源单据类型 ; SourceBillType : 0-出库单 , 1-入库单", example = "2")
    @ExcelProperty("来源单据类型")
    private Integer sourceBillType;

    @Schema(description = "参考号")
    @ExcelProperty("参考号")
    private String referNo;

    @Schema(description = "跟踪号")
    @ExcelProperty("跟踪号")
    private String traceNo;

    @Schema(description = "运输方式 ; ShippingMethod : 0-海运 , 1-铁路 , 2-空运 , 3-集卡")
    @ExcelProperty("运输方式")
    private Integer shippingMethod;

    @Schema(description = "预计到货时间")
    @ExcelProperty("预计到货时间")
    private LocalDateTime planArrivalTime;

    @Schema(description = "实际到货时间")
    @ExcelProperty("实际到货时间")
    private LocalDateTime actualArrivalTime;

    @Schema(description = "特别说明，创建方专用")
    @ExcelProperty("特别说明")
    private String creatorComment;

    @Schema(description = "初始库龄")
    @ExcelProperty("初始库龄")
    private Integer initAge;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "创建人姓名", example = "张三")
    @ExcelProperty("创建人姓名")
    private String creatorName;

    @Schema(description = "更新人姓名", example = "李四")
    @ExcelProperty("更新人姓名")
    private String updaterName;

    @Schema(description = "创建者", example = "")
    @ExcelProperty("创建者")
    private String creator;

    @Schema(description = "租户编号", example = "")
    @ExcelProperty("租户编号")
    private Long tenantId;

    @Schema(description = "更新时间", example = "")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "更新者", example = "")
    @ExcelProperty("更新者")
    private String updater;

    @Schema(description = "详情清单", example = "")
    @ExcelProperty("详情清单")
    private List<WmsInboundItemRespVO> itemList;
}
