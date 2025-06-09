package cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author jisencai
 * @table-fields : tenant_id,country,creator,code,contact_phone,create_time,city,contact_person,postcode,is_sync,updater,mode,external_storage_id,update_time,address_line2,province,address_line1,address_line3,name,id,status
 */
@Schema(description = "管理后台 - 仓库 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsWarehouseSimpleRespVO {

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

    @Schema(description = "状态，WMS通用的对象有效状态 ; ValidStatus : 0-不可用 , 1-可用", example = "")
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "国家")
    @ExcelProperty("国家")
    private String country;
}
