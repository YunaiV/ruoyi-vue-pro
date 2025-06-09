package cn.iocoder.yudao.module.tms.controller.admin.port.info.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.tms.enums.TmsDictTypeConstants;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - TMS港口信息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class TmsPortInfoRespVO {

    @Schema(description = "编号")
    @ExcelProperty("编号")
    private Integer id;

    @Schema(description = "创建人")
    @ExcelProperty("创建人")
    private String creator;

    @Schema(description = "创建人名称")
    @ExcelProperty("创建人名称")
    private String creatorName;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新人")
    @ExcelProperty("更新人")
    private String updater;

    @Schema(description = "更新人名称")
    @ExcelProperty("更新人名称")
    private String updaterName;

    @Schema(description = "更新时间")
    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "港口编码")
    @ExcelProperty("港口编码")
    private String code;

    @Schema(description = "港口中文名")
    @ExcelProperty("港口中文名")
    private String name;

    @Schema(description = "港口英文名")
    @ExcelProperty("港口英文名")
    private String nameEn;

    @Schema(description = "国家代码(字典)")
    @ExcelProperty(value = "国家代码", converter = DictConvert.class)
    @DictFormat(TmsDictTypeConstants.COUNTRY_CODE)
    private Integer countryCode;

    @Schema(description = "国家描述")
    @ExcelProperty("国家描述")
    private String countryName;

    @Schema(description = "城市中文名")
    @ExcelProperty("城市中文名")
    private String cityName;

    @Schema(description = "城市英文名")
    @ExcelProperty("城市英文名")
    private String cityNameEn;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "启用/禁用状态")
    @ExcelProperty(value = "启用/禁用状态", converter = DictConvert.class)
    @DictFormat(TmsDictTypeConstants.BOOLEAN_STATUS)
    private Boolean status;

}