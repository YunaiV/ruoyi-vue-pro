package cn.iocoder.yudao.module.crm.controller.admin.product.vo.product;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

// TODO @zange-ok：按照需求，裁剪下筛选的字段，目前应该只要 name 和 status
@Schema(description = "管理后台 - 产品分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmProductPageReqVO extends PageParam {

    @Schema(description = "产品名称", example = "李四")
    private String name;

    @Schema(description = "产品编码")
    private String no;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "价格", example = "8911")
    private Long price;

    @Schema(description = "状态", example = "2")
    private Integer status;

    @Schema(description = "产品分类ID", example = "1738")
    private Long categoryId;

    @Schema(description = "产品描述", example = "你说的对")
    private String description;

    @Schema(description = "负责人的用户编号", example = "31926")
    private Long ownerUserId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
