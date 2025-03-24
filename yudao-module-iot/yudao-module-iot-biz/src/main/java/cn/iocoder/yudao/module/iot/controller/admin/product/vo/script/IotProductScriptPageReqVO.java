package cn.iocoder.yudao.module.iot.controller.admin.product.vo.script;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.enums.product.IotProductScriptLanguageEnum;
import cn.iocoder.yudao.module.iot.enums.product.IotProductScriptStatusEnum;
import cn.iocoder.yudao.module.iot.enums.product.IotProductScriptTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - IoT 产品脚本信息分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotProductScriptPageReqVO extends PageParam {

    @Schema(description = "产品ID", example = "28277")
    private Long productId;

    @Schema(description = "产品唯一标识符")
    private String productKey;

    @Schema(description = "脚本类型", example = "1")
    @InEnum(IotProductScriptTypeEnum.class)
    private Integer scriptType;

    @Schema(description = "脚本语言")
    @InEnum(IotProductScriptLanguageEnum.class)
    private String scriptLanguage;

    @Schema(description = "状态", example = "0")
    @InEnum(IotProductScriptStatusEnum.class)
    private Integer status;

    @Schema(description = "备注说明", example = "你说的对")
    private String remark;

    @Schema(description = "最后测试时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] lastTestTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}