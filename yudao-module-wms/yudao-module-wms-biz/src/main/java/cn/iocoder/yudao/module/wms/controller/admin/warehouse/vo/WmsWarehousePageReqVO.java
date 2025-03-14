package cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : country,code,contact_phone,create_time,city,contact_person,postcode,is_sync,mode,external_storage_id,address_line2,province,address_line1,company_name,name,status
 */
@Schema(description = "管理后台 - 仓库分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsWarehousePageReqVO extends PageParam {

    @Schema(description = "仓库经营方式 ; WarehouseMode : 0-自营 , 1-三方仓 , 2-平台仓")
    private Integer mode;

    @Schema(description = "代码")
    private String code;

    @Schema(description = "名称", example = "李四")
    private String name;

    @Schema(description = "外部存储ID", example = "22814")
    private Long externalStorageId;

    @Schema(description = "外部存储代码")
    private String externalStorageCode;

    @Schema(description = "公司名称", example = "张三")
    private String companyName;

    @Schema(description = "国家")
    private String country;

    @Schema(description = "省/州")
    private String province;

    @Schema(description = "市")
    private String city;

    @Schema(description = "详细地址1")
    private String addressLine1;

    @Schema(description = "详细地址2")
    private String addressLine2;

    @Schema(description = "邮编")
    private String postcode;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系的话")
    private String contactPhone;

    @Schema(description = "库存同步：0-关闭；1-开启；")
    private Integer isSync;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "状态，WMS通用的对象有效状态 ; ValidStatus : 0-不可用 , 1-可用", example = "")
    private Integer status;
}
