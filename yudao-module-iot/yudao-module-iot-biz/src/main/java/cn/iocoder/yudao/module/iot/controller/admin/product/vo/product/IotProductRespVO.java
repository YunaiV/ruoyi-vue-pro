package cn.iocoder.yudao.module.iot.controller.admin.product.vo.product;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.iot.enums.DictTypeConstants;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 产品 Response VO")
@Data
@ExcelIgnoreUnannotated
public class IotProductRespVO {

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "26087")
    @ExcelProperty("产品编号")
    private Long id;

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("产品名称")
    private String name;

    @Schema(description = "产品标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("产品标识")
    private String productKey;

    @Schema(description = "产品分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long categoryId;

    @Schema(description = "产品分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("产品分类")
    private String categoryName;

    @Schema(description = "产品图标", example = "https://iocoder.cn/1.svg")
    @ExcelProperty("产品图标")
    private String icon;

    @Schema(description = "产品图片", example = "https://iocoder.cn/1.png")
    @ExcelProperty("产品图片")
    private String picUrl;

    @Schema(description = "产品描述", example = "你猜")
    @ExcelProperty("产品描述")
    private String description;

    @Schema(description = "产品状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "产品状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.PRODUCT_STATUS)
    private Integer status;

    @Schema(description = "设备类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty(value = "设备类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.PRODUCT_DEVICE_TYPE)
    private Integer deviceType;

    @Schema(description = "联网方式", example = "2")
    @ExcelProperty(value = "联网方式", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.NET_TYPE)
    private Integer netType;

    @Schema(description = "定位方式", example = "2")
    @ExcelProperty(value = "定位方式", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.LOCATION_TYPE)
    private Integer locationType;

    @Schema(description = "数据格式", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty(value = "数据格式", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.CODEC_TYPE)
    private String codecType;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
