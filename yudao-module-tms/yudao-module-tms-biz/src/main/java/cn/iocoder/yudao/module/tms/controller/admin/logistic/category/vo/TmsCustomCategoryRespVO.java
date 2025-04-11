package cn.iocoder.yudao.module.tms.controller.admin.logistic.category.vo;

import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.item.vo.TmsCustomCategoryItemRespVO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 海关分类 Response VO")
@Data
@ExcelIgnoreUnannotated
public class TmsCustomCategoryRespVO {

    @Schema(description = "编号")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "材质-字典")
    @ExcelProperty("材质-字典")
    private Integer material;

    @Schema(description = "报关品名")
    @ExcelProperty("报关品名")
    private String declaredType;

    @Schema(description = "英文品名")
    @ExcelProperty("英文品名")
    private String declaredTypeEn;

    @Schema(description = "材质对应string+报关品名")
    @ExcelProperty("材质对应string+报关品名")
    private String combinedValue;

    //对应产品数量
    @Schema(description = "对应产品数量")
    @ExcelProperty("对应产品数量")
    private Long productCount;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "最后更新时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime updateTime;

    @Schema(description = "创建人")
    @ExcelProperty("创建人")
    private String creator;

    @Schema(description = "最后更新人")
    @ExcelProperty("最后更新人")
    private String updater;

    @Schema(description = "海关分类子表列表")
    private List<TmsCustomCategoryItemRespVO> customRuleCategoryItems;
}