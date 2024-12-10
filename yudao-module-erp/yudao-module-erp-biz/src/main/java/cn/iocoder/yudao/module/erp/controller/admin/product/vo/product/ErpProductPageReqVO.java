package cn.iocoder.yudao.module.erp.controller.admin.product.vo.product;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 产品分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpProductPageReqVO extends PageParam {

    @Schema(description = "产品名称", example = "张三")
    private String name;

    @Schema(description = "产品分类编号", example = "30796")
    private Long categoryId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "部门id", example = "8369")
    private Long deptId;

    @Schema(description = "SKU（编码）")
    private String barCode;

    @Schema(description = "单位编号", example = "30975")
    private Long unitId;

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
    private BigDecimal width;

    @Schema(description = "基础长度（mm）")
    private BigDecimal length;

    @Schema(description = "基础高度（mm）")
    private BigDecimal height;

    @Schema(description = "图片url", example = "https://www.iocoder.cn")
    private String imageUrl;

    @Schema(description = "指导价，json格式", example = "19540")
    private String guidePrice;

    @Schema(description = "专利")
    private String patent;

    @Schema(description = "PO产品经理id", example = "28770")
    private Long productManagerId;

    @Schema(description = "ID工业设计id", example = "27774")
    private Long industrialDesignerId;

    @Schema(description = "RD研发工程师id", example = "10321")
    private Long researchDeveloperId;

    @Schema(description = "维护工程师id", example = "22163")
    private Long maintenanceEngineerId;

    @Schema(description = "层板承重")
    private BigDecimal shelfLoadCapacity;

    @Schema(description = "层板数量", example = "352")
    private Integer shelvesCount;

    @Schema(description = "电视调节方式")
    private String tvAdjustmentMethod;

    @Schema(description = "层板调节方式")
    private String shelfAdjustmentMethod;

    @Schema(description = "设计说明", example = "你说的对")
    private String description;

    @Schema(description = "宽度最大值")
    private BigDecimal widthMax;

    @Schema(description = "宽度最小值")
    private BigDecimal widthMin;

    @Schema(description = "长度最大值")
    private BigDecimal lengthMax;

    @Schema(description = "长度最小值")
    private BigDecimal lengthMin;

}