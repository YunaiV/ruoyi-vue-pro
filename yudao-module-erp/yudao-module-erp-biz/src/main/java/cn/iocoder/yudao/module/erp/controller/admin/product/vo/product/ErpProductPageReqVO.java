package cn.iocoder.yudao.module.erp.controller.admin.product.vo.product;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 产品分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpProductPageReqVO extends PageParam {

    @Schema(description = "产品名称", example = "电视机")
    private String name;

    @Schema(description = "产品分类编号")
    private Long categoryId;

    @Schema(description = "创建时间(\"yyyy-MM-dd HH:mm:ss\")")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "部门id")
    private Long deptId;

    @Schema(description = "SKU（编码）")
    private String barCode;

    @Schema(description = "单位编号")
    private Long unitId;

    @Schema(description = "品牌",example = "you")
    private String brand;

    @Schema(description = "材料（中文）")
    private String material;

    @Schema(description = "产品状态（1启用，0禁用）", example = "1")
    private Boolean status;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

    @Schema(description = "基础重量（kg）")
    private BigDecimal weight;

    @Schema(description = "系列")
    private String series;

    @Schema(description = "颜色")
    private String color;

    @Schema(description = "型号")
    private String model;

    @Schema(description = "流水号")
    private Integer serial;

    @Schema(description = "生产编号")
    private String productionNo;

    @Schema(description = "基础宽度（mm）")
    private Integer width;

    @Schema(description = "基础长度（mm）")
    private Integer length;

    @Schema(description = "基础高度（mm）")
    private Integer height;

    @Schema(description = "PO产品经理id")
    private Long productOwnerId;

    @Schema(description = "ID工业设计id")
    private Long industrialDesignerId;

    @Schema(description = "RD研发工程师id")
    private Long researchDeveloperId;

    @Schema(description = "维护工程师id")
    private Long maintenanceEngineerId;

}