package cn.iocoder.yudao.module.system.controller.admin.dict.vo.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(title = "管理后台 - 数据字典精简 Response VO")
@Data
public class DictDataSimpleRespVO {

    @Schema(title = "字典类型", required = true, example = "gender")
    private String dictType;

    @Schema(title = "字典键值", required = true, example = "1")
    private String value;

    @Schema(title = "字典标签", required = true, example = "男")
    private String label;

    @Schema(title = "颜色类型", example = "default", description = "default、primary、success、info、warning、danger")
    private String colorType;
    @Schema(title = "css 样式", example = "btn-visible")
    private String cssClass;

}
