package cn.iocoder.yudao.module.iot.controller.admin.product.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

// TODO @haohao：涉及到 iot 的拼写，要不都用 IoT，貌似更规范
// TODO 芋艿：需要清理掉一些无用字段
@Schema(description = "管理后台 - iot 产品分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductPageReqVO extends PageParam {

    @Schema(description = "产品名称", example = "李四")
    private String name;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "产品标识")
    private String productKey;

    @Schema(description = "接入网关协议", example = "2")
    private Integer protocolType;

    @Schema(description = "协议编号（脚本解析 id）", example = "13177")
    private Long protocolId;

    @Schema(description = "产品所属品类标识符", example = "14237")
    private Long categoryId;

    @Schema(description = "产品描述", example = "你猜")
    private String description;

    @Schema(description = "数据校验级别", example = "1")
    private Integer validateType;

    @Schema(description = "产品状态", example = "1")
    private Integer status;

    @Schema(description = "设备类型", example = "2")
    private Integer deviceType;

    @Schema(description = "联网方式", example = "2")
    private Integer netType;

    @Schema(description = "数据格式", example = "0")
    private Integer dataFormat;

}