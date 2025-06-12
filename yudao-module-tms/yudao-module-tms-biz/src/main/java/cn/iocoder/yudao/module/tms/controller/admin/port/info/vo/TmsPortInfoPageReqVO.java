package cn.iocoder.yudao.module.tms.controller.admin.port.info.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - TMS港口信息分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TmsPortInfoPageReqVO extends PageParam {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "创建人")
    private String creator;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "更新人")
    private String updater;

    @Schema(description = "更新时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] updateTime;

    @Schema(description = "港口编码")
    private String code;

    @Schema(description = "港口中文名")
    private String name;

    @Schema(description = "港口英文名")
    private String nameEn;

    @Schema(description = "国家代码(字典)")
    private Integer countryCode;

    @Schema(description = "国家描述")
    private String countryName;

    @Schema(description = "城市中文名")
    private String cityName;

    @Schema(description = "城市英文名")
    private String cityNameEn;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "启用/禁用状态")
    private Boolean status;

}