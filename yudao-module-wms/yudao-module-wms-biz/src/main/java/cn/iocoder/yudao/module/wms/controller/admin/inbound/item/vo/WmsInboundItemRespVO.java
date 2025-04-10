package cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo;

import cn.iocoder.yudao.module.wms.controller.admin.dept.DeptSimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundSimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.bin.vo.WmsWarehouseBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehouseSimpleRespVO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : tenant_id,outbound_available_qty,creator,inbound_status,notes,company_id,create_time,plan_qty,shelved_qty,latest_flow_id,updater,inbound_id,source_item_id,update_time,actual_qty,product_id,id,dept_id
 */
@Schema(description = "管理后台 - 入库单详情 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsInboundItemRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "30520")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "仓库ID", example = "23620")
    @ExcelProperty("仓库ID")
    private Long warehouseId;

    @Schema(description = "仓位ID", example = "23620")
    @ExcelProperty("仓位ID")
    private Long binId;

    @Schema(description = "入库单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "29327")
    @ExcelProperty("入库单ID")
    private Long inboundId;

    @Schema(description = "标准产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "27659")
    @ExcelProperty("标准产品ID")
    private Long productId;

    @Schema(description = "来源详情ID", example = "30830")
    @ExcelProperty("来源详情ID")
    private Long sourceItemId;

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

    @Schema(description = "产品", example = "")
    @ExcelProperty("产品")
    private WmsProductRespSimpleVO product;

    @Schema(description = "入库状态 ; WmsInboundStatus : 0-未入库 , 1-部分入库 , 2-已入库", example = "")
    @ExcelProperty("入库状态")
    private Integer inboundStatus;

    @Schema(description = "入库单", example = "")
    @ExcelProperty("入库单")
    private WmsInboundSimpleRespVO inbound;

    @Schema(description = "实际入库量", example = "")
    @ExcelProperty("实际入库量")
    private Integer actualQty;

    @Schema(description = "库龄", example = "")
    @ExcelProperty("库龄")
    private Integer age;

    @Schema(description = "批次剩余库存，出库后的剩余库存量", example = "")
    @ExcelProperty("批次剩余库存")
    private Integer outboundAvailableQty;

    @Schema(description = "计划入库量", example = "")
    @ExcelProperty("计划入库量")
    private Integer planQty;

    @Schema(description = "已上架量，已经拣货到仓位的库存量", example = "")
    @ExcelProperty("已上架量")
    private Integer shelvedQty;

    @Schema(description = "最新的流水ID", example = "")
    @ExcelProperty("最新的流水ID")
    private Long latestFlowId;

    @Schema(description = "仓库", example = "")
    private WmsWarehouseSimpleRespVO warehouse;

    @Schema(description = "库位", example = "")
    private WmsWarehouseBinRespVO bin;

    @Schema(description = "库存归属部门ID", example = "")
    @ExcelProperty("库存归属部门ID")
    private Long deptId;

    @Schema(description = "备注", example = "")
    @ExcelProperty("备注")
    private String notes;

    @Schema(description = "部门", example = "")
    @ExcelProperty("部门")
    private DeptSimpleRespVO dept;

    @Schema(description = "库存财务公司ID", example = "")
    @ExcelProperty("库存财务公司ID")
    private Long companyId;
}
