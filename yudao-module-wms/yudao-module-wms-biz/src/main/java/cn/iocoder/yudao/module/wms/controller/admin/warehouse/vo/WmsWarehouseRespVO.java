package cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : tenant_id,country,creator,code,contact_phone,create_time,city,contact_person,postcode,is_sync,updater,mode,external_storage_id,update_time,address_line2,province,address_line1,company_name,name,id,status
 */
@Schema(description = "管理后台 - 仓库 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsWarehouseRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "30946")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "仓库经营方式 ; WarehouseMode : 0-自营 , 1-三方仓 , 2-平台仓", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("仓库经营方式")
    private Integer mode;

    @Schema(description = "代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("代码")
    private String code;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("名称")
    private String name;

    @Schema(description = "外部存储ID", example = "22814")
    @ExcelProperty("外部存储ID")
    private Long externalStorageId;

    @Schema(description = "公司名称", example = "张三")
    @ExcelProperty("公司名称")
    private String companyName;

    @Schema(description = "国家")
    @ExcelProperty("国家")
    private String country;

    @Schema(description = "省/州")
    @ExcelProperty("省/州")
    private String province;

    @Schema(description = "市")
    @ExcelProperty("市")
    private String city;

    @Schema(description = "详细地址1")
    @ExcelProperty("详细地址1")
    private String addressLine1;

    @Schema(description = "详细地址2")
    @ExcelProperty("详细地址2")
    private String addressLine2;

    @Schema(description = "邮编")
    @ExcelProperty("邮编")
    private String postcode;

    @Schema(description = "联系人")
    @ExcelProperty("联系人")
    private String contactPerson;

    @Schema(description = "联系的话")
    @ExcelProperty("联系的话")
    private String contactPhone;

    @Schema(description = "库存同步：0-关闭；1-开启；")
    @ExcelProperty("库存同步")
    private Integer isSync;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "创建人姓名", example = "张三")
    @ExcelProperty("创建人姓名")
    private String creatorName;

    @Schema(description = "更新人姓名", example = "李四")
    @ExcelProperty("更新人姓名")
    private String updaterName;

    @Schema(description = "创建者", example = "")
    @ExcelProperty("创建者")
    private String creator;

    @Schema(description = "更新者", example = "")
    @ExcelProperty("更新者")
    private String updater;

    @Schema(description = "更新时间", example = "")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "租户编号", example = "")
    @ExcelProperty("租户编号")
    private Long tenantId;

    @Schema(description = "状态，WMS通用的对象有效状态 ; ValidStatus : 0-不可用 , 1-可用", example = "")
    @ExcelProperty("状态")
    private Integer status;
}
