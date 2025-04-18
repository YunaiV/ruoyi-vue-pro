package cn.iocoder.yudao.module.wms.controller.admin.external.storage.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : api_parameters,code,create_time,name,type,status
 */
@Schema(description = "管理后台 - 外部存储库分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsExternalStoragePageReqVO extends PageParam {

    @Schema(description = "代码")
    private String code;

    @Schema(description = "名称", example = "芋艿")
    private String name;

    @Schema(description = "外部仓类型 ; ExternalStorageType : 1-三方仓 , 2-平台仓", example = "1")
    private Integer type;

    @Schema(description = "对接参数，JSON格式的对接需要的参数")
    private String apiParameters;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "状态，WMS通用的对象有效状态 ; ValidStatus : 0-不可用 , 1-可用", example = "")
    private Integer status;
}
