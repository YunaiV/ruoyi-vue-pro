package cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @table-fields : tenant_id,outbound_available_qty,creator,inbound_status,company_id,create_time,plan_qty,shelve_closed_qty,upstream_id,remark,latest_flow_id,updater,inbound_id,update_time,actual_qty,product_id,id,dept_id
 */
@Schema(description = "管理后台 - 盘点单产品详情 Import VO")
@Data
@ExcelIgnoreUnannotated
public class WmsStockBinMoveImportVO {

    @Schema(description = "Excel 文件", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Excel 文件不能为空")
    private MultipartFile file;


}
